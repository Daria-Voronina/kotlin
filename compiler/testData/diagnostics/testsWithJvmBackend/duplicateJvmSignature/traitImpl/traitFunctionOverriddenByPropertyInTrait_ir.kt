// IGNORE_FIR
// TARGET_BACKEND: JVM_IR

interface T {
    fun getX() = 1
}

interface C : T {
    val x: Int
        <!ACCIDENTAL_OVERRIDE!>get()<!> = 1
}
