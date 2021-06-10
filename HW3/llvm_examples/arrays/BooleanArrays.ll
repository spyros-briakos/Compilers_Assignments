@.BooleanArrays_vtable = global [0 x i8*] []

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
@_cNSZ = constant [15 x i8] c"Negative size\0a\00"
define void @print_int(i32 %i) {
    %_str = bitcast [4 x i8]* @_cint to i8*
    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
    ret void
}

define void @throw_oob() {
    %_str = bitcast [15 x i8]* @_cOOB to i8*
    call i32 (i8*, ...) @printf(i8* %_str)
    call void @exit(i32 1)
    ret void
}

define void @throw_nsz() {
    %_str = bitcast [15 x i8]* @_cNSZ to i8*
    call i32 (i8*, ...) @printf(i8* %_str)
    call void @exit(i32 1)
    ret void
}

define i32 @main() {
    %b = alloca i8*

    %_0 = add i32 4, 10
    %_1 = icmp sge i32 %_0, 4
    br i1 %_1, label %nsz_ok_0, label %nsz_err_0

    nsz_err_0:
    call void @throw_nsz()
    br label %nsz_ok_0

    nsz_ok_0:
    %_2 = call i8* @calloc(i32 1, i32 %_0)
    %_3 = bitcast i8* %_2 to i32*
    store i32 10, i32* %_3
    store i8* %_2, i8** %b

    %_4 = load i8*, i8** %b
    %_6 = bitcast i8* %_4 to i32*
    %_5 = load i32, i32* %_6
    %_7 = icmp sge i32 0, 0
    %_8 = icmp slt i32 0, %_5
    %_9 = and i1 %_7, %_8
    br i1 %_9, label %oob_ok_0, label %oob_err_0

    oob_err_0:
    call void @throw_oob()
    br label %oob_ok_0

    oob_ok_0:
    %_10 = add i32 4, 0
    %_12 = zext i1 1 to i8
    %_11 = getelementptr i8, i8* %_4, i32 %_10
    store i8 %_12, i8* %_11

    %_13 = load i8*, i8** %b
    %_15 = bitcast i8* %_13 to i32*
    %_14 = load i32, i32* %_15
    %_16 = icmp sge i32 1, 0
    %_17 = icmp slt i32 1, %_14
    %_18 = and i1 %_16, %_17
    br i1 %_18, label %oob_ok_1, label %oob_err_1

    oob_err_1:
    call void @throw_oob()
    br label %oob_ok_1

    oob_ok_1:
    %_19 = add i32 4, 1
    %_20 = getelementptr i8, i8* %_13, i32 %_19
    %_21 = load i8, i8* %_20
    %_22 = trunc i8 %_21 to i1

    br i1 %_22, label %if_then_0, label %if_else_0
    if_else_0:
    call void (i32) @print_int(i32 2)
    br label %if_end_0
    if_then_0:
    call void (i32) @print_int(i32 1)
    br label %if_end_0
    if_end_0:


    ret i32 0
}

