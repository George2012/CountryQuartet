# Country Quartet R8 rules.
#
# The app uses no reflection, no serialization library and no dynamic class
# loading: the game content is parsed field by field with org.json, and every
# model is constructed by our own code. The rules that AGP, Compose, Kotlin and
# DataStore contribute are therefore enough, and nothing here exists to work
# around a crash.
#
# Kept deliberately small: a blanket "-keep class com.countryquartet.**" would
# switch off shrinking for the whole app and hide real problems.

# Line numbers in any crash report, without exposing original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
