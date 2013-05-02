def sum(a: Int, b:Int):Int =  a + b
def fib(f: (Int, Int) => Int)(a: Int): Int = {
    if (a <= 0)  0
    else if (a == 1) 1
    else f(fib(f)( a - 1), fib(f)(a - 2));
}

def fib2(f: (Int, Int) => Int)(a: Int): Int = a match {
    case 0|1 => a
    case _ => f(fib2(f)( a - 1), fib2(f)(a - 2));
}


for (i <- 0 until 10) println(fib(sum)(i))
(0 to 10) map  (fib2((x,y) => x + y)(_)) foreach println

