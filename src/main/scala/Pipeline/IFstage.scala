/**************************/
//Instruction Fetch stage//
/**************************/
package Pipeline

import Pipeline.consts.ExcCode
import Pipeline.consts.PCsrc._
import chisel3._
import chisel3.util.MuxCase

//需要传给下一级(ID)的信号
class IFtoIDIo extends Bundle{
  val IF_pcplus4 = Output(UInt(32.W))
  val IF_epc = Output(UInt(32.W))
  val IF_inst = Output(UInt(32.W))
  val IF_BD = Output(UInt(1.W))
  // val IF_isBr_Jp  = Output(UInt(1.W))
  val ID_isBr_Jp = Input(UInt(1.W))

}

class IFtoImemIo extends Bundle{
  val rd_data = Input(UInt(32.W))

  val wr = Output(UInt(1.W))
  val addr = Output(UInt(32.W))
  val wr_data = Output(UInt(32.W))
}

//IF级的输入输出
class IFstageIo extends Bundle{
  val IF_ID = new IFtoIDIo
  val IF_Imem = new IFtoImemIo
  val IF_Exc = Flipped(new ExceptionToMEM)

  val IFwr = Input(UInt(1.W))
  val IF_flush = Input(UInt(1.W))
  val PCwr = Input(UInt(1.W))
  val PCsrc = Input(UInt(2.W))

  val pc_jump = Input(UInt(32.W))
  val pc_br = Input(UInt(32.W))

  val test_wr = Input(UInt(1.W))


}


class IFstage extends Module {
  val io = IO(new IFstageIo)

  //两个流水级寄存器
  val IF_inst = RegInit(UInt(32.W),0.U)
  val IF_pcplus4 = RegInit(UInt(32.W),0.U)
  val IF_EPC = RegInit(UInt(32.W),0.U)

  val IF_BD = RegInit(UInt(1.W),0.U)

  val pc = RegInit(UInt(32.W),0.U)

  val pc_p4 = Wire(UInt(32.W))
  val epc = WireInit(0.U(32.W))



  when(io.IF_Exc.flush.toBool()){

    IF_inst:=0.U
    IF_pcplus4:=0.U
    IF_EPC:= 0.U
    IF_BD:=0.U
    pc:="x000040b0".U(32.W)

  }.otherwise({

    when(io.PCwr.toBool()){
      //pc三选一
      pc :=Mux(io.test_wr.toBool(),0.U, MuxCase(pc_p4,Array(
        (io.PCsrc===sel_pcplus4)->     pc_p4,
        (io.PCsrc===sel_br)     ->  io.pc_br,
        (io.PCsrc===sel_jump)   ->io.pc_jump
      )))
    }

    when(io.IFwr.toBool()){
      when(io.IF_flush.toBool()){
        IF_pcplus4 := 0.U
        IF_inst := 0.U
      }.otherwise({
        IF_pcplus4 := pc_p4
        IF_inst := io.IF_Imem.rd_data
      })
    }
    IF_EPC:= epc
    IF_BD:=io.IF_ID.ID_isBr_Jp
  })

  pc_p4 := pc + 4.U
  epc := Mux(io.IF_ID.ID_isBr_Jp.toBool(),pc-4.U,pc)


  io.IF_Exc.ExcCode:=ExcCode.AdEL
  io.IF_Exc.epc:= epc
  io.IF_Exc.BD:=io.IF_ID.ID_isBr_Jp
  io.IF_Exc.badVaddr:=pc


  when(pc(1,0)=/=0.U(2.W)){
    io.IF_Exc.ExcHappen := 1.U
  }.otherwise({
    io.IF_Exc.ExcHappen := 0.U
  })



  io.IF_Imem.wr := 0.U
  io.IF_Imem.addr := pc>>2
  io.IF_Imem.wr_data:=0.U

  //传给下一级的数据，从寄存器中取值
  io.IF_ID.IF_inst := IF_inst
  io.IF_ID.IF_pcplus4 := IF_pcplus4
  io.IF_ID.IF_epc := IF_EPC
  io.IF_ID.IF_BD:=IF_BD

  //log
//  printf("IF: pc=0x%x,pc+4=0x%x,inst=0x%x\n",pc,pc_p4,io.IF_ID.IF_inst)

}
