#define I2C_TROUBLE_3

#include <jni.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <linux/i2c-dev.h>
#include "i2cbusses.h"
#include "util.h"
#include "../version.h"

#include <limits.h>
#include <dirent.h>
#include <fcntl.h>
#include <errno.h>

#define RANGE_SENSOR_ADDRESS "0x13"  // 0x26 write, 0x27 read
#define COLOR_SENSOR_ADDRESS "0x2a"
#define GYRO_SENSOR_ADDRESS "0x68"

// VCNL4000 Register Map
#define COMMAND_0 "0x80"  // starts measurments, relays data ready info
#define PRODUCT_ID "0x81"  // product ID/revision ID, should read 0x11
#define IR_CURRENT "0x83"  // sets IR current in steps of 10mA 0-200mA
#define AMBIENT_PARAMETER "0x84"  // Configures ambient light measures
#define AMBIENT_RESULT_MSB "0x85"  // high byte of ambient light measure
#define AMBIENT_RESULT_LSB "0x86"  // low byte of ambient light measure
#define PROXIMITY_RESULT_MSB "0x87"  // High byte of proximity measure
#define PROXIMITY_RESULT_LSB "0x88"  // low byte of proximity measure
#define PROXIMITY_FREQ "0x89"  // Proximity IR test signal freq, 0-3
#define PROXIMITY_MOD "0x8A"  // proximity modulator timing

void local_I2cRangeSensorInit( int lr );
void writeByte( char* port, char* addr1, char* addr2, char* value );
int readByte( char* port, char* addr1, char* addr2 );
int setI2c(int argc, char *argv[]);
int getI2c(int argc, char *argv[]);

void i2cLog( char* arg1, char* arg2, char* arg3 ){
#ifdef I2C_LOG
	__android_log_write(arg1, arg2, arg3);
#endif
}

static void help(void)
{
	fprintf(stderr,
	        "Usage: i2cset [-f] [-y] [-m MASK] I2CBUS CHIP-ADDRESS DATA-ADDRESS [VALUE [MODE]]\n"
		"  I2CBUS is an integer or an I2C bus name\n"
		"  ADDRESS is an integer (0x03 - 0x77)\n"
		"  MODE is one of:\n"
		"    b (byte, default)\n"
		"    w (word)\n"
		"    Append p for SMBus PEC\n");
	exit(1);
}

static int check_setFuncs(int file, int size, int pec)
{
	unsigned long funcs;

	/* check adapter functionality */
	if (ioctl(file, I2C_FUNCS, &funcs) < 0) {
		fprintf(stderr, "Error: Could not get the adapter "
			"functionality matrix: %s\n", strerror(errno));
		return -1;
	}

	switch (size) {
	case I2C_SMBUS_BYTE:
		if (!(funcs & I2C_FUNC_SMBUS_WRITE_BYTE)) {
			fprintf(stderr, MISSING_FUNC_FMT, "SMBus send byte");
			return -1;
		}
		break;

	case I2C_SMBUS_BYTE_DATA:
		if (!(funcs & I2C_FUNC_SMBUS_WRITE_BYTE_DATA)) {
			fprintf(stderr, MISSING_FUNC_FMT, "SMBus write byte");
			return -1;
		}
		break;

	case I2C_SMBUS_WORD_DATA:
		if (!(funcs & I2C_FUNC_SMBUS_WRITE_WORD_DATA)) {
			fprintf(stderr, MISSING_FUNC_FMT, "SMBus write word");
			return -1;
		}
		break;
	}

	if (pec
	 && !(funcs & (I2C_FUNC_SMBUS_PEC | I2C_FUNC_I2C))) {
		fprintf(stderr, "Warning: Adapter does "
			"not seem to support PEC\n");
	}

	return 0;
}

static int check_getFuncs(int file, int size, int daddress, int pec)
{
	unsigned long funcs;

	/* check adapter functionality */
	if (ioctl(file, I2C_FUNCS, &funcs) < 0) {
		fprintf(stderr, "Error: Could not get the adapter "
			"functionality matrix: %s\n", strerror(errno));
		return -1;
	}

	switch (size) {
	case I2C_SMBUS_BYTE:
		if (!(funcs & I2C_FUNC_SMBUS_READ_BYTE)) {
			fprintf(stderr, MISSING_FUNC_FMT, "SMBus receive byte");
			return -1;
		}
		if (daddress >= 0
		 && !(funcs & I2C_FUNC_SMBUS_WRITE_BYTE)) {
			fprintf(stderr, MISSING_FUNC_FMT, "SMBus send byte");
			return -1;
		}
		break;

	case I2C_SMBUS_BYTE_DATA:
		if (!(funcs & I2C_FUNC_SMBUS_READ_BYTE_DATA)) {
			fprintf(stderr, MISSING_FUNC_FMT, "SMBus read byte");
			return -1;
		}
		break;

	case I2C_SMBUS_WORD_DATA:
		if (!(funcs & I2C_FUNC_SMBUS_READ_WORD_DATA)) {
			fprintf(stderr, MISSING_FUNC_FMT, "SMBus read word");
			return -1;
		}
		break;
	}

	if (pec
	 && !(funcs & (I2C_FUNC_SMBUS_PEC | I2C_FUNC_I2C))) {
		fprintf(stderr, "Warning: Adapter does "
			"not seem to support PEC\n");
	}

	return 0;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cSensorInit( JNIEnv* env, jobject thiz )
{
	i2cLog( "I2C", "I2c", "I2cSensorInit: Start");

//	__android_log_write("MTC", "before chmod ", "");
//    chmod( "/dev/ttyUSB0", "u+x" );
//    __android_log_write("MTC", "after chmod", "");
//	system("su -c chmod 777 /dev/ttyUSB0");
	system("/system/bin/sh /chinit.sh");

	//Gyro Sensor
	writeByte( "2", GYRO_SENSOR_ADDRESS, "0x6b", "0x00");
	i2cLog( "I2C", "I2c", "I2cSensorInit: #1");

	//Range Sensor
	i2cLog( "I2C", "I2c", "I2cSensorInit: #2");
	writeByte( "2", RANGE_SENSOR_ADDRESS, AMBIENT_PARAMETER, "0x0F");	// Single conversion mode, 128 averages
	writeByte( "2", RANGE_SENSOR_ADDRESS, IR_CURRENT, "20");  			// Set IR current to 200mA
	writeByte( "2", RANGE_SENSOR_ADDRESS, PROXIMITY_FREQ, "2");			// 781.25 kHz
	writeByte( "2", RANGE_SENSOR_ADDRESS, PROXIMITY_MOD, "0x81");		// 129, recommended by Vishay
	i2cLog( "I2C", "I2c", "I2cSensorInit: #3");
#ifndef I2C_TROUBLE_3
	writeByte( "3", RANGE_SENSOR_ADDRESS, AMBIENT_PARAMETER, "0x0F");	// Single conversion mode, 128 averages
	writeByte( "3", RANGE_SENSOR_ADDRESS, IR_CURRENT, "20");  			// Set IR current to 200mA
	writeByte( "3", RANGE_SENSOR_ADDRESS, PROXIMITY_FREQ, "2");			// 781.25 kHz
	writeByte( "3", RANGE_SENSOR_ADDRESS, PROXIMITY_MOD, "0x81");		// 129, recommended by Vishay
	i2cLog( "I2C", "I2c", "I2cSensorInit: #4");
#endif

	//Color Sensor
//	i2cLog( "I2C", "I2c", "I2cSensorInit: #5");
//	writeByte( "2", COLOR_SENSOR_ADDRESS, "0x00", "0x8a");
//	i2cLog( "I2C", "I2c", "I2cSensorInit: #6");
//	writeByte( "2", COLOR_SENSOR_ADDRESS, "0x00", "0x2a");
//	i2cLog( "I2C", "I2c", "I2cSensorInit: #7");
#ifndef I2C_TROUBLE_3
	writeByte( "3", COLOR_SENSOR_ADDRESS, "0x00", "0x8a");
	i2cLog( "I2C", "I2c", "I2cSensorInit: #8");
	writeByte( "3", COLOR_SENSOR_ADDRESS, "0x00", "0x2a");
#endif

	i2cLog( "I2C", "I2c", "I2cSensorInit: End");
	return 1;
}

void writeByte( char* port, char* addr1, char* addr2, char* value ){
	char arg0[] = {"0"};
	char arg1[] = {"-y"};
	char *argv[] = { arg0, arg1, port, addr1, addr2, value };
	setI2c( 6, argv );
}

int readByte( char* port, char* addr1, char* addr2 ){
	char arg0[] = {"0"};
	char arg1[] = {"-y"};
	char *argv[] = { arg0, arg1, port, addr1, addr2 };
	int ret = getI2c( 5, argv );
	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cGetRange( JNIEnv* env, jobject thiz, jint lr )
{
	int ret = -1;
    unsigned int data, temp;
	char port[] = {"2"};
	char tempStr[8];
	int readVal = 0;
	if( lr != 0 ){
		port[0] = '3';
	}

#ifdef I2C_TROUBLE_3
	port[0] = '2';
#endif

	temp = readByte( port, RANGE_SENSOR_ADDRESS, COMMAND_0 ) | 0x08;
	sprintf(tempStr, "%#x", temp);
	writeByte(port, RANGE_SENSOR_ADDRESS, COMMAND_0, tempStr);  // command the sensor to perform a proximity measure

	while(!(readByte(port, RANGE_SENSOR_ADDRESS, COMMAND_0) & 0x20))
	  ;  // Wait for the proximity data ready bit to be set
	data = readByte(port, RANGE_SENSOR_ADDRESS, PROXIMITY_RESULT_MSB) << 8;
	data |= readByte(port, RANGE_SENSOR_ADDRESS, PROXIMITY_RESULT_LSB);
	ret = data;

	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cColorStatus( JNIEnv* env, jobject thiz, jint lr )
{
	int ret = 0;
	int r,g,b,a;
	char ci2cbus[64];
	char port[] = {"2"};
	if( lr != 0 ){
		port[0] = '3';
	}

#ifdef I2C_TROUBLE_3
	port[0] = '2';
#endif

	r = (readByte( port, COLOR_SENSOR_ADDRESS, "0x03" ) << 8) |
		readByte( port, COLOR_SENSOR_ADDRESS, "0x04" );
	g = (readByte( port, COLOR_SENSOR_ADDRESS, "0x05" ) << 8) |
		readByte( port, COLOR_SENSOR_ADDRESS, "0x06" );
	b = (readByte( port, COLOR_SENSOR_ADDRESS, "0x07" ) << 8) |
		readByte( port, COLOR_SENSOR_ADDRESS, "0x08" );
	a = (readByte( port, COLOR_SENSOR_ADDRESS, "0x09" ) << 8) |
		readByte( port, COLOR_SENSOR_ADDRESS, "0x0a" );

//	sprintf(ci2cbus, "R:%d, G:%d, B:%d, A:%d, V:%d", r, g, b, a, (r+g+b)*100/a);
//	__android_log_write("I2C", "I2C", ci2cbus);

//	if( (r+g+b)*100/a > white ){
//		ret = 0;	//White
//	}else if( (r+g+b)*100/a < black ){
//		ret = 1;	//Black
//	}else{
//		ret = 2;	//Glay
//	}
	if( (r+g+b) > 0 ){
		ret = (r+g+b)*100/a;
	}

	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cGetColor( JNIEnv* env, jobject thiz, jint lr ,jint color )
{
	int ret = 0;
    unsigned int data, temp;
	char port[] = {"2"};
	int readVal = 0;
	int r,g,b,a;
	char ci2cbus[16];
	if( lr != 0 ){
		port[0] = '3';
	}

#ifdef I2C_TROUBLE_3
	port[0] = '2';
#endif

	if( color == 0 ){
		ret = (readByte( port, COLOR_SENSOR_ADDRESS, "0x03" ) << 8) |
			readByte( port, COLOR_SENSOR_ADDRESS, "0x04" );
	}else if( color == 1 ){
		ret = (readByte( port, COLOR_SENSOR_ADDRESS, "0x05" ) << 8) |
			readByte( port, COLOR_SENSOR_ADDRESS, "0x06" );
	}else if( color == 2 ){
		ret = (readByte( port, COLOR_SENSOR_ADDRESS, "0x07" ) << 8) |
			readByte( port, COLOR_SENSOR_ADDRESS, "0x08" );
	}else if( color == 3 ){
		ret = (readByte( port, COLOR_SENSOR_ADDRESS, "0x09" ) << 8) |
			readByte( port, COLOR_SENSOR_ADDRESS, "0x0a" );
	}

	sprintf(ci2cbus, "R:%d, G:%d, B:%d, A:%d", r, g, b, a);
	i2cLog( "I2C", "I2c", ci2cbus);

	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cGetAccelX( JNIEnv* env, jobject thiz )
{
	int ret = 0;
    unsigned int data, temp;
	char port[] = {"2"};

	ret = (readByte( port, GYRO_SENSOR_ADDRESS, "0x3b" ) << 8) |
		  readByte( port, GYRO_SENSOR_ADDRESS, "0x3c" );

	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cGetAccelY( JNIEnv* env, jobject thiz )
{
	int ret = 0;
    unsigned int data, temp;
	char port[] = {"2"};

	ret = (readByte( port, GYRO_SENSOR_ADDRESS, "0x3d" ) << 8) |
		  readByte( port, GYRO_SENSOR_ADDRESS, "0x3e" );

	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cGetAccelZ( JNIEnv* env, jobject thiz )
{
	int ret = 0;
    unsigned int data, temp;
	char port[] = {"2"};

	ret = (readByte( port, GYRO_SENSOR_ADDRESS, "0x3f" ) << 8) |
		  readByte( port, GYRO_SENSOR_ADDRESS, "0x40" );

	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cGetGyroX( JNIEnv* env, jobject thiz )
{
	int ret = 0;
    unsigned int data, temp;
	char port[] = {"2"};

	ret = (readByte( port, GYRO_SENSOR_ADDRESS, "0x43" ) << 8) |
		  readByte( port, GYRO_SENSOR_ADDRESS, "0x44" );

	return ret;
}

jint
Java_robo2014_sapporoid_SensorControl_I2cGetGyroY( JNIEnv* env, jobject thiz )
{
	int ret = 0;
    unsigned int data, temp;
	char port[] = {"2"};

	ret = (readByte( port, GYRO_SENSOR_ADDRESS, "0x45" ) << 8) |
		  readByte( port, GYRO_SENSOR_ADDRESS, "0x46" );

	return ret;
}

int setI2c(int argc, char *argv[])
{
	char *end;
	const char *maskp = NULL;
	int res, i2cbus, address, size, file;
	int value, daddress, vmask = 0;
	char filename[20];
	int pec = 0;
	int flags = 0;
	int force = 0, yes = 0, version = 0, readback = 0;

	i2cLog( "I2C", "I2c", "I2cSet: Start");

	/* handle (optional) flags first */
	while (1+flags < argc && argv[1+flags][0] == '-') {
		i2cLog( "I2C", "I2c", "I2cSet: #1");
		switch (argv[1+flags][1]) {
		case 'V': version = 1; break;
		case 'f': force = 1; break;
		case 'y': yes = 1; break;
		case 'm':
			if (2+flags < argc)
				maskp = argv[2+flags];
			flags++;
			break;
		case 'r': readback = 1; break;
		default:
			fprintf(stderr, "Error: Unsupported option "
				"\"%s\"!\n", argv[1+flags]);
			help();
//			exit(1);
			return 0;
		}
		flags++;
	}
	i2cLog( "I2C", "I2c", "I2cSet: #2");

	if (version) {
		fprintf(stderr, "i2cset version %s\n", VERSION);
//		exit(0);
		return 0;
	}
	i2cLog( "I2C", "I2c", "I2cSet: #3");

	if (argc < flags + 4)
		help();
	i2cLog( "I2C", "I2c", "I2cSet: #4");

	i2cbus = lookup_i2c_bus(argv[flags+1]);
	if (i2cbus < 0)
		help();
	i2cLog( "I2C", "I2c", "I2cSet: #5");

	address = parse_i2c_address(argv[flags+2]);
	if (address < 0)
		help();
	i2cLog( "I2C", "I2c", "I2cSet: #6");

	daddress = strtol(argv[flags+3], &end, 0);
	if (*end || daddress < 0 || daddress > 0xff) {
		fprintf(stderr, "Error: Data address invalid!\n");
		help();
	}
	i2cLog( "I2C", "I2c", "I2cSet: #7");

	if (argc > flags + 4) {
		size = I2C_SMBUS_BYTE_DATA;
		value = strtol(argv[flags+4], &end, 0);
		if (*end || value < 0) {
			fprintf(stderr, "Error: Data value invalid!\n");
			help();
		}
	} else {
		size = I2C_SMBUS_BYTE;
		value = -1;
	}
	i2cLog( "I2C", "I2c", "I2cSet: #8");

	if (argc > flags + 5) {
		switch (argv[flags+5][0]) {
		case 'b': size = I2C_SMBUS_BYTE_DATA; break;
		case 'w': size = I2C_SMBUS_WORD_DATA; break;
		default:
			fprintf(stderr, "Error: Invalid mode!\n");
			help();
		}
		pec = argv[flags+5][1] == 'p';
	}
	i2cLog( "I2C", "I2c", "I2cSet: #9");

	/* Old method to provide the value mask, deprecated and no longer
	   documented but still supported for compatibility */
	if (argc > flags + 6) {
		if (maskp) {
			fprintf(stderr, "Error: Data value mask provided twice!\n");
			help();
		}
		fprintf(stderr, "Warning: Using deprecated way to set the data value mask!\n");
		fprintf(stderr, "         Please switch to using -m.\n");
		maskp = argv[flags+6];
	}
	i2cLog( "I2C", "I2c", "I2cSet: #10");

	if (maskp) {
		vmask = strtol(maskp, &end, 0);
		if (*end || vmask == 0) {
			fprintf(stderr, "Error: Data value mask invalid!\n");
			help();
		}
	}
	i2cLog( "I2C", "I2c", "I2cSet: #11");

	if ((size == I2C_SMBUS_BYTE_DATA && value > 0xff)
	 || (size == I2C_SMBUS_WORD_DATA && value > 0xffff)) {
		fprintf(stderr, "Error: Data value out of range!\n");
		help();
	}	i2cLog( "I2C", "I2c", "I2cSet: #12");

	file = open_i2c_dev(i2cbus, filename, 0);
	if (file < 0
	 || check_setFuncs(file, size, pec)
	 || set_slave_addr(file, address, force))
//		exit(1);
		return 0;
	i2cLog( "I2C", "I2c", "I2cSet: #13");

//	if (!yes && !confirm(filename, address, size, daddress,
//			     value, vmask, pec))
//		exit(0);

	if (vmask) {
		int oldvalue;

		switch (size) {
		case I2C_SMBUS_BYTE:
			oldvalue = i2c_smbus_read_byte(file);
			break;
		case I2C_SMBUS_WORD_DATA:
			oldvalue = i2c_smbus_read_word_data(file, daddress);
			break;
		default:
			oldvalue = i2c_smbus_read_byte_data(file, daddress);
		}
		i2cLog( "I2C", "I2c", "I2cSet: #14");

		if (oldvalue < 0) {
			fprintf(stderr, "Error: Failed to read old value\n");
//			exit(1);
			return 0;
		}

		value = (value & vmask) | (oldvalue & ~vmask);

		if (!yes) {
			fprintf(stderr, "Old value 0x%0*x, write mask "
				"0x%0*x: Will write 0x%0*x to register "
				"0x%02x\n",
				size == I2C_SMBUS_WORD_DATA ? 4 : 2, oldvalue,
				size == I2C_SMBUS_WORD_DATA ? 4 : 2, vmask,
				size == I2C_SMBUS_WORD_DATA ? 4 : 2, value,
				daddress);

			fprintf(stderr, "Continue? [Y/n] ");
			fflush(stderr);
			if (!user_ack(1)) {
				fprintf(stderr, "Aborting on user request.\n");
//				exit(0);
				return 0;
			}
		}
	}
	i2cLog( "I2C", "I2c", "I2cSet: #15");

	if (pec && ioctl(file, I2C_PEC, 1) < 0) {
		fprintf(stderr, "Error: Could not set PEC: %s\n",
			strerror(errno));
		close(file);
//		exit(1);
		return 0;
	}
	i2cLog( "I2C", "I2c", "I2cSet: #16");

	switch (size) {
	case I2C_SMBUS_BYTE:
		res = i2c_smbus_write_byte(file, daddress);
		break;
	case I2C_SMBUS_WORD_DATA:
		res = i2c_smbus_write_word_data(file, daddress, value);
		break;
	default: /* I2C_SMBUS_BYTE_DATA */
		res = i2c_smbus_write_byte_data(file, daddress, value);
	}
	if (res < 0) {
		fprintf(stderr, "Error: Write failed\n");
		close(file);
//		exit(1);
		return 0;
	}
	i2cLog( "I2C", "I2c", "I2cSet: #17");

	if (pec) {
		if (ioctl(file, I2C_PEC, 0) < 0) {
			fprintf(stderr, "Error: Could not clear PEC: %s\n",
				strerror(errno));
			close(file);
//			exit(1);
			return 0;
		}
	}
	i2cLog( "I2C", "I2c", "I2cSet: #18");

	if (!readback) { /* We're done */
		close(file);
//		exit(0);
		return 0;
	}

	switch (size) {
	case I2C_SMBUS_BYTE:
		res = i2c_smbus_read_byte(file);
		value = daddress;
		break;
	case I2C_SMBUS_WORD_DATA:
		res = i2c_smbus_read_word_data(file, daddress);
		break;
	default: /* I2C_SMBUS_BYTE_DATA */
		res = i2c_smbus_read_byte_data(file, daddress);
	}
	close(file);
	i2cLog( "I2C", "I2c", "I2cSet: #19");

	if (res < 0) {
		printf("Warning - readback failed\n");
	} else
	if (res != value) {
		printf("Warning - data mismatch - wrote "
		       "0x%0*x, read back 0x%0*x\n",
		       size == I2C_SMBUS_WORD_DATA ? 4 : 2, value,
		       size == I2C_SMBUS_WORD_DATA ? 4 : 2, res);
	} else {
		printf("Value 0x%0*x written, readback matched\n",
		       size == I2C_SMBUS_WORD_DATA ? 4 : 2, value);
	}

	i2cLog( "I2C", "I2c", "I2cSet: End");
//	exit(0);
	return 1;
}

//Java_robo_servotester_ServoControl_I2cGet( JNIEnv* env, jobject thiz, jstring port, jstring addr, jstring num )
int getI2c(int argc, char *argv[])
{
	char *end;
	int res, i2cbus, address, size, file;
	int daddress;
	char filename[20];
	int pec = 0;
	int flags = 0;
	int force = 0, yes = 0, version = 0;
//	jint* aArray;
	int retFlag = 0;
	int retValue = 0;

	i2cLog( "I2C", "I2c", "I2cGet: Start");
//    aArray = (*env)->GetIntArrayElements(env, arr, NULL);
	jboolean b;
//	int argc = 5;
//	char argv[6][2] = {{'0','0'}, {'-','y'}, {'2', 0x00}, {0x68, 0x00}, {0x6b, 0x00}, {0x00, 0x00}};
//	char arg0[] = {"0"};
//	char arg1[] = {"-y"};
//	char arg2[] = {"2"};
//	char arg3[] = {"0x68"};
//	char arg4[] = {"0x3b"};
//	char *arg2 = (*env)->GetStringUTFChars(env, port, &b);
//	char *arg3 = (*env)->GetStringUTFChars(env, addr, &b);
//	char *arg4 = (*env)->GetStringUTFChars(env, num, &b);
//
//	char *argv[] = { arg0, arg1, arg2, arg3, arg4 };

	/* handle (optional) flags first */
	while ( retFlag == 0 && 1+flags < argc && argv[1+flags][0] == '-') {
		switch (argv[1+flags][1]) {
		case 'V':
			version = 1;
			i2cLog( "I2C", "I2c", "I2cGet: #1");
			break;
		case 'f':
			force = 1;
			i2cLog( "I2C", "I2c", "I2cGet: #2");
			break;
		case 'y':
			yes = 1;
			i2cLog( "I2C", "I2c", "I2cGet: #3");
			break;
		default:
			i2cLog( "I2C", "I2c", "I2cGet: #4");
			fprintf(stderr, "Error: Unsupported option "
				"\"%s\"!\n", argv[1+flags]);
			help();
//			exit(1);
			retFlag = 1;
		}
		flags++;
	}

	if (version) {
		i2cLog( "I2C", "I2c", "I2cGet: #5");
		fprintf(stderr, "i2cget version %s\n", VERSION);
//		exit(0);
		retFlag = 1;
	}

	if( retFlag == 0 ){
		i2cLog( "I2C", "I2c", "I2cGet: #6");
		if (argc < flags + 3){
			i2cLog( "I2C", "I2c", "I2cGet: #7");
			help();
		}

		i2cbus = lookup_i2c_bus(argv[flags+1]);
		if (i2cbus < 0){
			i2cLog( "I2C", "I2c", "I2cGet: #8");
			help();
		}

		address = parse_i2c_address(argv[flags+2]);
		if (address < 0){
			i2cLog( "I2C", "I2c", "I2cGet: #9");
			help();
		}

		if (argc > flags + 3) {
			i2cLog( "I2C", "I2c", "I2cGet: #10");
			size = I2C_SMBUS_BYTE_DATA;
			daddress = strtol(argv[flags+3], &end, 0);
			if (*end || daddress < 0 || daddress > 0xff) {
				i2cLog( "I2C", "I2c", "I2cGet: #11");
				fprintf(stderr, "Error: Data address invalid!\n");
				help();
			}
		} else {
			i2cLog( "I2C", "I2c", "I2cGet: #12");
			size = I2C_SMBUS_BYTE;
			daddress = -1;
		}

		if (argc > flags + 4) {
			i2cLog( "I2C", "I2c", "I2cGet: #13");
			switch (argv[flags+4][0]) {
			case 'b':
				size = I2C_SMBUS_BYTE_DATA;
				i2cLog( "I2C", "I2c", "I2cGet: #14");
				break;
			case 'w':
				size = I2C_SMBUS_WORD_DATA;
				i2cLog( "I2C", "I2c", "I2cGet: #15");
				break;
			case 'c':
				size = I2C_SMBUS_BYTE;
				i2cLog( "I2C", "I2c", "I2cGet: #16");
				break;
			default:
				i2cLog( "I2C", "I2c", "I2cGet: #17");
				fprintf(stderr, "Error: Invalid mode!\n");
				help();
			}
			pec = argv[flags+4][1] == 'p';
		}

		file = open_i2c_dev(i2cbus, filename, 0);
		if (file < 0 ){
			i2cLog( "I2C", "I2c", "I2cGet: #18-1");
			retFlag = 1;
		}

		if( check_getFuncs(file, size, daddress, pec) ){
			i2cLog( "I2C", "I2c", "I2cGet: #18-2");
			retFlag = 1;
		}

		if( set_slave_addr(file, address, force)){
			i2cLog( "I2C", "I2c", "I2cGet: #18-3");
//			exit(1);
			retFlag = 1;
		}

//		if (!yes && !confirm(filename, address, size, daddress, pec))
//			exit(0);

		if ( retFlag == 0 && pec && ioctl(file, I2C_PEC, 1) < 0) {
			i2cLog( "I2C", "I2c", "I2cGet: #19");
			fprintf(stderr, "Error: Could not set PEC: %s\n",
				strerror(errno));
			close(file);
//			exit(1);
			retFlag = 1;
		}

		if( retFlag == 0 ){
			switch (size) {
			case I2C_SMBUS_BYTE:
				i2cLog( "I2C", "I2c", "I2cGet: #20");
				if (daddress >= 0) {
					res = i2c_smbus_write_byte(file, daddress);
					if (res < 0)
						fprintf(stderr, "Warning - write failed\n");
				}
				res = i2c_smbus_read_byte(file);
				break;
			case I2C_SMBUS_WORD_DATA:
				i2cLog( "I2C", "I2c", "I2cGet: #21");
				res = i2c_smbus_read_word_data(file, daddress);
				break;
			default: /* I2C_SMBUS_BYTE_DATA */
				i2cLog( "I2C", "I2c", "I2cGet: #22");
				res = i2c_smbus_read_byte_data(file, daddress);
			}
			close(file);

			if (res < 0) {
				i2cLog( "I2C", "I2c", "I2cGet: #23");
				fprintf(stderr, "Error: Read failed\n");
//				exit(2);
				retFlag = 1;
			}
		}
//		printf("0x%0*x\n", size == I2C_SMBUS_WORD_DATA ? 4 : 2, res);
		retValue = res;
		{
			char ci2cbus[10];
			sprintf(ci2cbus, "%d", retValue);
			i2cLog( "I2C", "I2c", ci2cbus);
		}
	}

//	(*env)->ReleaseIntArrayElements(env, arr, aArray, 0);
//	(*env)->ReleaseStringArrayElements(env, arr, aArray, 0);

	i2cLog( "I2C", "I2c", "I2cGet: End");
//	(*env)->ReleaseStringUTFChars( env, port, arg2 );
//	(*env)->ReleaseStringUTFChars( env, addr, arg3 );
//	(*env)->ReleaseStringUTFChars( env, num, arg4);

//	exit(0);
	return retValue;
}
