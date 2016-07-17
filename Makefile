APP=./app/build/outputs/apk/app-debug.apk
APP_NAME=net.programmierecke.radiodroid2
MAIN_CLASS=ActivityMain

.PHONY: all clean install uninstall build
all: build

build :
	./gradlew assembleDebug

# XXX : Sometimes the build process is stuck at 85%. A workarround is to redo a full build
rebuild :
	rm -rf build/ app/build
	./gradlew assembleDebug

test :
	./gradlew test --info

clean :
	./gradlew clean

install:
	adb install -r $(APP)

run: install
	adb shell am start -n $(APP_NAME)/$(APP_NAME).$(MAIN_CLASS)

uninstall:
	adb uninstall $(APP_NAME)

cleardata:
	adb shell pm clear fr.avalun

log:
	adb logcat -v time | grep `adb shell ps | grep $(APP_NAME) | cut -c10-15`

clearlog:
	adb logcat -c

stack:
	adb shell dumpsys activity | grep -i run. | grep $(APP_NAME)

# Useful for debug purpose

lsapk:
	unzip -l app/build/outputs/apk/app-debug.apk |less
