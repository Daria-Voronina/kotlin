package kotlin

@AnnotationWithInt(Int.MAX_VALUE)
class TestClassInPlatform

enum class TestEnumInPlatform {
    D, E, F
}

fun initCauseInPlatform() = Throwable().initCause(Throwable()) // `initCause` is not visible in `common` but visible in `platform`

@Target(AnnotationTarget.TYPE)
@MustBeDocumented
public annotation class ExtensionFunctionType

fun any() = Any()
fun string() = String()
fun boolean() = true
fun int() = 42
