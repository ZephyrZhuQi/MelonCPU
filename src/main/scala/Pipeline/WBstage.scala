package Pipeline

import Pipeline.consts.RegSrc
import chisel3._
import chisel3.util.MuxCase

class WBIo extends Bundle{
  val WB_MEM = Flipped(new MEMtoWBIo)

  val MEM_wbData = Output(UInt(32.W))

}

class WBstage extends Module{
  val io = IO(new WBIo)
  val wbData = WireInit(0.U(32.W))

  io.MEM_wbData:=wbData
  io.WB_MEM.wbData:=io.MEM_wbData

  when(io.WB_MEM.MEM_RegSrc===RegSrc.MEM_ALUout){
    wbData:=io.WB_MEM.MEM_ALUout
  }.otherwise({
    wbData:=io.WB_MEM.MEM_data
  })
  io.MEM_wbData:=wbData
  io.WB_MEM.wbData:=wbData



}
