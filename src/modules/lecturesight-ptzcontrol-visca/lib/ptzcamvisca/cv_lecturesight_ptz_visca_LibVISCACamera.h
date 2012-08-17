/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cv_lecturesight_ptz_visca_LibVISCACamera */

#ifndef _Included_cv_lecturesight_ptz_visca_LibVISCACamera
#define _Included_cv_lecturesight_ptz_visca_LibVISCACamera
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    initialize
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_initialize
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    deinitialize
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_deinitialize
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getConnectedCams
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getConnectedCams
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPower
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPower
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setIrLed
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setIrLed
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setZoomStop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomStop
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setZoomTeleSpeed
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomTeleSpeed
  (JNIEnv *, jobject, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setZoomWideSpeed
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomWideSpeed
  (JNIEnv *, jobject, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setZoomValue
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setZoomValue
  (JNIEnv *, jobject, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltUp
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltUp
  (JNIEnv *, jobject, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltDown
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltDown
  (JNIEnv *, jobject, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltLeft
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLeft
  (JNIEnv *, jobject, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltRight
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltRight
  (JNIEnv *, jobject, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltUpLeft
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltUpLeft
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltUpRight
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltUpRight
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltDownLeft
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltDownLeft
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltDownRight
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltDownRight
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltStop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltStop
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltAbsolutePosition
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltAbsolutePosition
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltRelativePosition
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltRelativePosition
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltHome
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltHome
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltReset
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltReset
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltLimitUpRight
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitUpRight
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltLimitDownLeft
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitDownLeft
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltLimitDownLeftClear
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitDownLeftClear
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    setPanTiltLimitUprightClear
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_setPanTiltLimitUprightClear
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getVendor
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getVendor
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getModel
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getModel
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getROMVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getROMVersion
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getPower
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getPower
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getIrLed
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getIrLed
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getZoomValue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getZoomValue
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getFocusAuto
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getFocusAuto
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getFocusValue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getFocusValue
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getPanMaxSpeed
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getPanMaxSpeed
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getPanPosition
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getPanPosition
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getTiltMaxSpeed
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getTiltMaxSpeed
  (JNIEnv *, jobject);

/*
 * Class:     cv_lecturesight_ptz_visca_LibVISCACamera
 * Method:    getTiltPosition
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_cv_lecturesight_ptz_visca_LibVISCACamera_getTiltPosition
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
