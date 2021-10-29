#import "jawt.h"
#import "jawt_md.h"
#import <Cocoa/Cocoa.h>
#import <QuartzCore/QuartzCore.h>


extern "C" void* objc_autoreleasePoolPush(void);
extern "C" void objc_autoreleasePoolPop(void*);

extern "C"
{

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_NativeApplicationKt_getApplicationWindowCount(JNIEnv *env, jobject obj)
{
    @autoreleasepool {
        return [[[NSApplication sharedApplication] windows] count];
    }
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_NativeApplicationKt_autoreleasePoolPush(
    JNIEnv * env, jobject redrawer)
{
    return (jlong)objc_autoreleasePoolPush();
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_NativeApplicationKt_autoreleasePoolPop(
    JNIEnv * env, jobject redrawer, jlong handle)
{
    objc_autoreleasePoolPop((void*)handle);
}

}


