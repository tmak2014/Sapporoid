/*
    i2cset.c - A user-space program to write an I2C register.
    Copyright (C) 2001-2003  Frodo Looijaard <frodol@dds.nl>, and
                             Mark D. Studebaker <mdsxyz123@yahoo.com>
    Copyright (C) 2004-2008  Jean Delvare <khali@linux-fr.org>

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
    MA 02110-1301 USA.
*/

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

static void help(void) __attribute__ ((noreturn));

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

static int check_funcs(int file, int size, int pec)
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

static int confirm(const char *filename, int address, int size, int daddress,
		   int value, int vmask, int pec)
{
	int dont = 0;

	fprintf(stderr, "WARNING! This program can confuse your I2C "
		"bus, cause data loss and worse!\n");

	if (address >= 0x50 && address <= 0x57) {
		fprintf(stderr, "DANGEROUS! Writing to a serial "
			"EEPROM on a memory DIMM\nmay render your "
			"memory USELESS and make your system "
			"UNBOOTABLE!\n");
		dont++;
	}

	fprintf(stderr, "I will write to device file %s, chip address "
		"0x%02x, data address\n0x%02x, ", filename, address, daddress);
	if (size == I2C_SMBUS_BYTE)
		fprintf(stderr, "no data.\n");
	else
		fprintf(stderr, "data 0x%02x%s, mode %s.\n", value,
			vmask ? " (masked)" : "",
			size == I2C_SMBUS_BYTE_DATA ? "byte" : "word");
	if (pec)
		fprintf(stderr, "PEC checking enabled.\n");

	fprintf(stderr, "Continue? [%s] ", dont ? "y/N" : "Y/n");
	fflush(stderr);
	if (!user_ack(!dont)) {
		fprintf(stderr, "Aborting on user request.\n");
		return 0;
	}

	return 1;
}

void i2cLog( char* arg1, char* arg2, char* arg3 ){
#ifdef I2C_LOG
	__android_log_write(arg1, arg2, arg3);
#endif
}

//int main(int argc, char *argv[])
jint
Java_robo_sapporoid_ServoControl_I2cSet( JNIEnv* env, jobject thiz, jint count, jobjectArray objects )
{
	char *end;
	const char *maskp = NULL;
	int res, i2cbus, address, size, file;
	int value, daddress, vmask = 0;
	char filename[20];
	int pec = 0;
	int flags = 0;
	int force = 0, yes = 0, version = 0, readback = 0;
	int argc = count + 1;
    char *argv[argc];
    int i;
    argv[0] = "0";
    jstring stringValue[argc];

	for( i=0; i<argc-1; i++ ){
	    stringValue[i+1] = (*env)->GetObjectArrayElement(env, objects, i);
	    argv[i+1] = (*env)->GetStringUTFChars(env, stringValue[i+1], NULL);
		i2cLog( "I2C", "I2c", argv[i+1]);
	}
//	char arg0[] = {"0"};
//	char arg1[] = {"-y"};
//	char arg2[] = {"2"};
//	char arg3[] = {"0x68"};
//	char arg4[] = {"0x6b"};
//	char arg5[] = {"0"};
//	char *argv[] = { arg0, arg1, arg2, arg3, arg4, arg5 };

	i2cLog( "I2C", "I2c", "I2cSet: Start");
	/* handle (optional) flags first */
	while (1+flags < argc && argv[1+flags][0] == '-') {
		i2cLog( "I2C", "I2c", argv[1+flags]);
		switch (argv[1+flags][1]) {
		case 'V':
			version = 1;
			i2cLog( "I2C", "I2c", "I2cSet: #1");
			break;
		case 'f':
			force = 1;
			i2cLog( "I2C", "I2c", "I2cSet: #2");
			break;
		case 'y':
			yes = 1;
			i2cLog( "I2C", "I2c", "I2cSet: #3");
			break;
		case 'm':
			if (2+flags < argc)
				maskp = argv[2+flags];
			flags++;
			i2cLog( "I2C", "I2c", "I2cSet: #4");
			break;
		case 'r':
			readback = 1;
			i2cLog( "I2C", "I2c", "I2cSet: #5");
			break;
		default:
			i2cLog( "I2C", "I2c", "I2cSet: #6");
			fprintf(stderr, "Error: Unsupported option "
				"\"%s\"!\n", argv[1+flags]);
			help();
//			exit(1);
			for( i=0; i<argc-1; i++ ){
				(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
			}
			return(0);
		}
		flags++;
	}
	i2cLog( "I2C", "I2c", argv[1+flags]);
	i2cLog( "I2C", "I2c", "I2cSet: #ext1");

	if (version) {
		i2cLog( "I2C", "I2c", "I2cSet: #7");
		fprintf(stderr, "i2cset version %s\n", VERSION);
//		exit(0);
		for( i=0; i<argc-1; i++ ){
			(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
		}
		return(0);
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext2");
	if (argc < flags + 4){
		help();
		i2cLog( "I2C", "I2c", "I2cSet: #8");
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext3");
	i2cLog( "I2C", "I2c", argv[flags+1]);
	i2cbus = lookup_i2c_bus(argv[flags+1]);
	if (i2cbus < 0){
		help();
		i2cLog( "I2C", "I2c", "I2cSet: #9");
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext4");
	address = parse_i2c_address(argv[flags+2]);
	if (address < 0){
		help();
		i2cLog( "I2C", "I2c", "I2cSet: #10");
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext5");
	daddress = strtol(argv[flags+3], &end, 0);
	if (*end || daddress < 0 || daddress > 0xff) {
		fprintf(stderr, "Error: Data address invalid!\n");
		help();
		i2cLog( "I2C", "I2c", "I2cSet: #11");
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext6");
	if (argc > flags + 4) {
		i2cLog( "I2C", "I2c", "I2cSet: #12");
		size = I2C_SMBUS_BYTE_DATA;
		value = strtol(argv[flags+4], &end, 0);
		if (*end || value < 0) {
			i2cLog( "I2C", "I2c", "I2cSet: #13");
			fprintf(stderr, "Error: Data value invalid!\n");
			help();
		}
	} else {
		i2cLog( "I2C", "I2c", "I2cSet: #14");
		size = I2C_SMBUS_BYTE;
		value = -1;
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext7");
	if (argc > flags + 5) {
		switch (argv[flags+5][0]) {
		case 'b':
			size = I2C_SMBUS_BYTE_DATA;
			i2cLog( "I2C", "I2c", "I2cSet: #15");
			break;
		case 'w':
			size = I2C_SMBUS_WORD_DATA;
			i2cLog( "I2C", "I2c", "I2cSet: #16");
			break;
		default:
			i2cLog( "I2C", "I2c", "I2cSet: #17");
			fprintf(stderr, "Error: Invalid mode!\n");
			help();
		}
		pec = argv[flags+5][1] == 'p';
	}

	/* Old method to provide the value mask, deprecated and no longer
	   documented but still supported for compatibility */
	i2cLog( "I2C", "I2c", "I2cSet: #ext8");
	if (argc > flags + 6) {
		i2cLog( "I2C", "I2c", "I2cSet: #18");
		if (maskp) {
			i2cLog( "I2C", "I2c", "I2cSet: #19");
			fprintf(stderr, "Error: Data value mask provided twice!\n");
			help();
		}
		fprintf(stderr, "Warning: Using deprecated way to set the data value mask!\n");
		fprintf(stderr, "         Please switch to using -m.\n");
		maskp = argv[flags+6];
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext9");
	if (maskp) {
		i2cLog( "I2C", "I2c", "I2cSet: #20");
		vmask = strtol(maskp, &end, 0);
		if (*end || vmask == 0) {
			i2cLog( "I2C", "I2c", "I2cSet: #21");
			fprintf(stderr, "Error: Data value mask invalid!\n");
			help();
		}
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext9");
	if ((size == I2C_SMBUS_BYTE_DATA && value > 0xff)
	 || (size == I2C_SMBUS_WORD_DATA && value > 0xffff)) {
		fprintf(stderr, "Error: Data value out of range!\n");
		help();
		i2cLog( "I2C", "I2c", "I2cSet: #22");
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext10");
	char ci2cbus[10];
	sprintf(ci2cbus, "%d", i2cbus);
//	file = open_i2c_dev(i2cbus, filename, 0);
	sprintf(filename, "/dev/i2c-%d", i2cbus);
	i2cLog( "I2C", "I2c", filename);
	file = open(filename, O_RDWR);
	sprintf(filename, "%d", file);
	i2cLog( "I2C", "I2c", filename);
//	if (file < 0
//	 || check_funcs(file, size, pec)
//	 || set_slave_addr(file, address, force))
////		exit(1);
//		i2cLog( "I2C", "I2c", "I2cSet: #23");
//		return(0);
	if (file < 0 ){
		i2cLog( "I2C", "I2c", "I2cSet: #test23-1");
		for( i=0; i<argc-1; i++ ){
			(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
		}
		return(0);
	}
	if( check_funcs(file, size, pec)){
		i2cLog( "I2C", "I2c", "I2cSet: #test23-2");
		for( i=0; i<argc-1; i++ ){
			(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
		}
		return(0);
	}
	if( set_slave_addr(file, address, force)){
		i2cLog( "I2C", "I2c", "I2cSet: #test23-3");
		for( i=0; i<argc-1; i++ ){
			(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
		}
		return(0);
	}

//	if (!yes && !confirm(filename, address, size, daddress,
//			     value, vmask, pec))
//		exit(0);

	i2cLog( "I2C", "I2c", "I2cSet: #ext11");
	if (vmask) {
		int oldvalue;

		switch (size) {
		case I2C_SMBUS_BYTE:
			oldvalue = i2c_smbus_read_byte(file);
			i2cLog( "I2C", "I2c", "I2cSet: #24");
			break;
		case I2C_SMBUS_WORD_DATA:
			oldvalue = i2c_smbus_read_word_data(file, daddress);
			i2cLog( "I2C", "I2c", "I2cSet: #25");
			break;
		default:
			oldvalue = i2c_smbus_read_byte_data(file, daddress);
			i2cLog( "I2C", "I2c", "I2cSet: #26");
		}

		if (oldvalue < 0) {
			i2cLog( "I2C", "I2c", "I2cSet: #27");
			fprintf(stderr, "Error: Failed to read old value\n");
//			exit(1);
			for( i=0; i<argc-1; i++ ){
				(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
			}
			return(1);
		}

		value = (value & vmask) | (oldvalue & ~vmask);

		if (!yes) {
			i2cLog( "I2C", "I2c", "I2cSet: #28");
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
				i2cLog( "I2C", "I2c", "I2cSet: #29");
				fprintf(stderr, "Aborting on user request.\n");
//				exit(0);
				for( i=0; i<argc-1; i++ ){
					(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
				}
				return(0);
			}
		}
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext12");
	if (pec && ioctl(file, I2C_PEC, 1) < 0) {
		i2cLog( "I2C", "I2c", "I2cSet: #30");
		fprintf(stderr, "Error: Could not set PEC: %s\n",
			strerror(errno));
		close(file);
//		exit(1);
		for( i=0; i<argc-1; i++ ){
			(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
		}
		return(0);
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext13");
	switch (size) {
	case I2C_SMBUS_BYTE:
		i2cLog( "I2C", "I2c", "I2cSet: #31");
		res = i2c_smbus_write_byte(file, daddress);
		break;
	case I2C_SMBUS_WORD_DATA:
		i2cLog( "I2C", "I2c", "I2cSet: #32");
		res = i2c_smbus_write_word_data(file, daddress, value);
		break;
	default: /* I2C_SMBUS_BYTE_DATA */
		i2cLog( "I2C", "I2c", "I2cSet: #33");
		res = i2c_smbus_write_byte_data(file, daddress, value);
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext14");
	if (res < 0) {
		i2cLog( "I2C", "I2c", "I2cSet: #34");
		fprintf(stderr, "Error: Write failed\n");
		close(file);
//		exit(1);
		for( i=0; i<argc-1; i++ ){
			(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
		}
		return(0);
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext15");
	if (pec) {
		i2cLog( "I2C", "I2c", "I2cSet: #35");
		if (ioctl(file, I2C_PEC, 0) < 0) {
			i2cLog( "I2C", "I2c", "I2cSet: #36");
			fprintf(stderr, "Error: Could not clear PEC: %s\n",
				strerror(errno));
			close(file);
//			exit(1);
			for( i=0; i<argc-1; i++ ){
				(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
			}
			return(0);
		}
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext16");
	if (!readback) { /* We're done */
		i2cLog( "I2C", "I2c", "I2cSet: #37");
		close(file);
//		exit(0);
		for( i=0; i<argc-1; i++ ){
			(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
		}
		return(1);
	}

	i2cLog( "I2C", "I2c", "I2cSet: #ext17");
	switch (size) {
	case I2C_SMBUS_BYTE:
		res = i2c_smbus_read_byte(file);
		value = daddress;
		i2cLog( "I2C", "I2c", "I2cSet: #38");
		break;
	case I2C_SMBUS_WORD_DATA:
		res = i2c_smbus_read_word_data(file, daddress);
		i2cLog( "I2C", "I2c", "I2cSet: #39");
		break;
	default: /* I2C_SMBUS_BYTE_DATA */
		res = i2c_smbus_read_byte_data(file, daddress);
		i2cLog( "I2C", "I2c", "I2cSet: #40");
	}
	close(file);

	i2cLog( "I2C", "I2c", "I2cSet: #ext18");
	if (res < 0) {
		i2cLog( "I2C", "I2c", "I2cSet: #41");
		printf("Warning - readback failed\n");
	} else
	if (res != value) {
		i2cLog( "I2C", "I2c", "I2cSet: #42");
		printf("Warning - data mismatch - wrote "
		       "0x%0*x, read back 0x%0*x\n",
		       size == I2C_SMBUS_WORD_DATA ? 4 : 2, value,
		       size == I2C_SMBUS_WORD_DATA ? 4 : 2, res);
	} else {
		i2cLog( "I2C", "I2c", "I2cSet: #43");
		printf("Value 0x%0*x written, readback matched\n",
		       size == I2C_SMBUS_WORD_DATA ? 4 : 2, value);
	}

	i2cLog( "I2C", "I2c", "I2cSet: End");
	for( i=0; i<argc-1; i++ ){
		(*env)->ReleaseStringUTFChars( env, stringValue[i+1], argv[i+1]);
	}
//	exit(0);
	return(1);
}
