package inline

inline fun f(): Int {
    A().foo.hashCode()
    return 0
}