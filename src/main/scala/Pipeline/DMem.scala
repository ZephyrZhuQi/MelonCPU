package Pipeline

import Pipeline.consts.MemWr
import chisel3.{util, _}

class DMemIo extends Bundle{

  val Addr = Input(UInt(32.W))
  val wr_data = Input(UInt(32.W))
  val MemRd = Input(UInt(1.W))
  val Memwr = Input(UInt(2.W))

  val out = Output(UInt(32.W))

}

class DMem extends Module {
  val io = IO(new DMemIo)
  val dmem = Mem(30000,UInt(32.W))

  val index_B = WireInit(0.U(2.W))
  val index_H = WireInit(0.U(1.W))
  val addr = WireInit(0.U(2.W))

  index_B := io.Addr(1,0)
  index_H := io.Addr(1)
  addr := (io.Addr>>2).asUInt()

  when(io.Memwr=/=MemWr.N){
    io.out:=0.U
    when(io.Memwr===MemWr.SB){
      when(index_B==="b00".U){

        dmem(addr):=util.Cat( dmem(addr)(31,8),io.wr_data(7,0) )

      }.elsewhen(index_B==="b01".U){

        dmem(addr):=util.Cat( dmem(addr)(31,16),io.wr_data(7,0), dmem(addr)(7,0) )

      }.elsewhen(index_B==="b10".U){

        dmem(addr):=util.Cat( dmem(addr)(31,24),io.wr_data(7,0), dmem(addr)(15,0) )

      }.otherwise({
        dmem(addr):=util.Cat(io.wr_data(7,0), dmem(addr)(23,0) )
      })
    }.elsewhen(io.Memwr===MemWr.SH){
      when(index_H==="b0".U){
        dmem(addr):= util.Cat(dmem(addr)(31,16),io.wr_data(15,0))

      }.otherwise({
        dmem(addr):=util.Cat(  io.wr_data(15,0),dmem(addr)(15,0))
      })
    }.otherwise({

      dmem(addr):=io.wr_data
    })
  }.elsewhen(io.MemRd.toBool()){
    io.out:=dmem(addr)
  }.otherwise({
    io.out:=0.U
  })

  //printf("DMem: data=0x%x,memwr=0x%x\n",io.wr_data,io.Memwr)
}
