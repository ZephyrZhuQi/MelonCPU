package Pipeline

import chisel3._
import Pipeline.consts.ABsrc._
import Pipeline.consts.RegWr

class BypassIO extends Bundle{
  val EX_dst = Input(UInt(5.W))
  val EX_RegWr = Input(UInt(2.W))
  val MEM_dst = Input(UInt(5.W))
  val MEM_RegWr = Input(UInt(2.W))
  val ID_Rs = Input(UInt(5.W))
  val ID_Rt = Input(UInt(5.W))

  val Asrc = Output(UInt(2.W))
  val Bsrc = Output(UInt(2.W))
}

class BypassUnit extends Module{
  val io = IO(new BypassIO)
//
//  io.Asrc:=A
//  io.Bsrc:=B
//
//  when(io.MEM_RegWr.toBool()){
//    when(io.ID_Rs===io.MEM_dst){
//      io.Asrc:=DataMem
//    }
//    when(io.ID_Rt===io.MEM_dst){
//      io.Bsrc:=DataMem
//    }
//  }



  when( io.EX_RegWr =/= RegWr.disable
    && io.ID_Rs===io.EX_dst&&io.EX_dst=/=0.U){
    io.Asrc:=DataEx
  }.elsewhen(io.MEM_RegWr=/=RegWr.disable
    && io.ID_Rs===io.MEM_dst&& io.MEM_dst=/=0.U){
    io.Asrc:=DataMem
  }.otherwise({
    io.Asrc:=A
  })

  when(io.EX_RegWr === RegWr.regfile && io.ID_Rt===io.EX_dst && io.EX_dst=/=0.U){
    io.Bsrc:=DataEx
  }.elsewhen(io.MEM_RegWr===RegWr.regfile && io.ID_Rt===io.MEM_dst && io.MEM_dst=/=0.U){
    io.Bsrc:=DataMem
  }.otherwise({
    io.Bsrc:=B
  })


//  printf("bypass: ID_Rs=0x%x,ID_Rt=0x%x,ExDst=0x%x,MemDst=0x%x,Asrc=0x%x,Bsrc=0x%x\n",io.ID_Rs,io.ID_Rt,io.EX_dst,io.MEM_dst,io.Asrc,io.Bsrc)



  //printf("Bypass : EX_",io.Asrc)
}