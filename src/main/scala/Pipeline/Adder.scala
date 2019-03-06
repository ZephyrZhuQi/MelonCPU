package Pipeline

import chisel3._
class Adder (val n: Int) extends Module {
  val io = IO(new Bundle{
    val A      = Input(UInt(n.W))
    val B      = Input(UInt(n.W))
    val Cin    = Input(UInt(1.W))
    val Sum    = Output(UInt(n.W))
    val Overflow = Output(UInt(1.W))
  })
  io.Sum := io.A+io.B+io.Cin
  io.Overflow :=((~io.A(n-3)).asUInt() & (~io.B(n-3)).asUInt()&io.Sum(n-3)) | (io.A(n-3)&io.B(n-3)&(~io.Sum(n-3)).asUInt())
}
