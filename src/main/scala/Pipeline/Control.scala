package Pipeline

import Pipeline.consts.ALUctr._
import Pipeline.consts.PCsrc
import chisel3._

class ControlIo extends Bundle {
  val ALUctr = Input(UInt(6.W))
  val cond = Input(UInt(1.W))

  val IF_flush = Output(UInt(1.W))
  val PCSrc = Output(UInt(2.W))
  val isJR = Output(UInt(1.W))
}

class Control extends Module {
  val io = IO(new ControlIo)

  when(
    (io.ALUctr === ctr_bne || io.ALUctr===ctr_beq ||
      io.ALUctr===ctr_bgez || io.ALUctr===ctr_bltz ||
      io.ALUctr===ctr_bgezal || io.ALUctr===ctr_bltzal||
      io.ALUctr===ctr_bgtz || io.ALUctr===ctr_blez)
    && io.cond.toBool()
  ) {
    io.IF_flush := 1.U
    io.PCSrc := PCsrc.sel_br
    io.isJR:=0.U
  }.elsewhen(io.ALUctr === ctr_jump || io.ALUctr===ctr_jal ||
    io.ALUctr===ctr_jr || io.ALUctr === ctr_jalr
  ) {
    when(io.ALUctr===ctr_jr || io.ALUctr===ctr_jalr ){

      io.isJR:=1.U
      io.IF_flush := 1.U
      io.PCSrc := PCsrc.sel_jump

    }.otherwise({
      io.isJR:=0.U
      io.IF_flush := 1.U
      io.PCSrc := PCsrc.sel_jump
    })
  }.otherwise({
      io.IF_flush := 0.U
      io.PCSrc := PCsrc.sel_pcplus4
      io.isJR:=0.U
    })



  //printf("Control: cmp=0x%x,aluctr=0x%x\n",io.cond,io.ALUctr)

}
