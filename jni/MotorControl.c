#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

//#define BAUDRATE B115200
//#define BAUDRATE B9600
//#define BAUDRATE 460800
#define BAUDRATE B57600
#define MODEMDEVICE "/dev/ttyUSB0"
#define FALSE 0
#define TRUE 1
#define BUFF_SIZE 128
#define BUFF_SIZE_SERVO 18

void aLog( char* arg1, char* arg2, char* arg3 ){
	__android_log_write(arg1, arg2, arg3);
}

jbyteArray
//Java_com_yamauchi_usbserial_WalkControler_Walk( JNIEnv* env, jobject thiz, jintArray arr )
Java_robo2014_sapporoid_Robo_Walk( JNIEnv* env, jobject thiz, jstring str1 )
{
	int fd,c, res;
	int buff_size = BUFF_SIZE;	struct termios oldtio,newtio;	char buf[BUFF_SIZE];    char readbuf[128] = {0};
//	jint* aArray;
	int i = -1;
	int n = 0;
	int count = 0;
	jbyte *dst;
	jbyteArray dstj;
	char logstr[64] = {0};
	jboolean b;
	const char *c1 = (*env)->GetStringUTFChars(env, str1, &b);
	int len;
	int pos = 0;

//    aArray = (*env)->GetIntArrayElements(env, arr, NULL);
//	fd = open(MODEMDEVICE, O_WRONLY | O_NOCTTY );
	fd = open(MODEMDEVICE, O_RDWR | O_NOCTTY );
	if (fd <0) {perror(MODEMDEVICE); exit(-1); }	
	tcgetattr(fd,&oldtio);
	
	bzero(&newtio, sizeof(newtio));	newtio.c_cflag = BAUDRATE | CRTSCTS | CS8 | CLOCAL | CREAD;
	newtio.c_iflag = IGNPAR | ICRNL;	newtio.c_oflag = 0;
	newtio.c_cc[VTIME]    = 0;	newtio.c_cc[VMIN]     = 1;
			tcflush(fd, TCIFLUSH);	tcsetattr(fd,TCSANOW, &newtio);
//	buf[0] = aArray[0];
//	buf[1] = ',';
//	buf[2] = aArray[1];
//	buf[3] = 10;
//	buff_size = 4;
	strcpy( &buf[0], c1 );
	buff_size = strlen(&buf[0]) + 1;
	buf[buff_size - 1] = 10;
	
	write(fd, buf, buff_size);
//	while( i <= 0 ){
	i = read(fd, readbuf, 128);

	dstj = (*env)->NewByteArray(env, 128);
	dst = (*env)->GetByteArrayElements(env, dstj, NULL);
	memcpy( &dst[0], &readbuf[0], 128 );
	len = strlen(&dst[0]);
	for( n=1; n<len; n++ ){
		if( dst[len - n] != 10 ){
			break;
		}
	}
	len -= (n - 1);

	if( dst[len] != 10 ){
		memset( &readbuf[0], 0x00, 128 );
		i = read(fd, readbuf, 128);
		memcpy( &dst[len], &readbuf[0], 128 - len );
	}
//	len = strlen(&dst[0]);
//	memset( &dst[len - 2], 0x00, 128 - ( len - 2) );

//	while( dst[len-1] != 10 ){
//		i = read(fd, readbuf, 128);
//		strcat( &dst[0], &readbuf[0] );
//		len = strlen(&dst[0]);
//	}

	close(fd);
	fd = 0;
	
//	dstj = (*env)->NewByteArray(env, 128);
//	dst = (*env)->GetByteArrayElements(env, dstj, NULL);
//	memcpy( &dst[0], &readbuf[0], 128 );
	(*env)->ReleaseByteArrayElements(env, dstj, dst, 0);
//	(*env)->ReleaseIntArrayElements(env, arr, aArray, 0);
	(*env)->ReleaseStringUTFChars(env, str1, c1);
	
	return dstj;
}
