package Pipeline

import chisel3._

class RegFile extends Module {
  val io = IO(new Bundle {
    val rd_addr1 = Input(UInt(5.W))
    val rd_addr2 = Input(UInt(5.W))
    val wr_addr = Input(UInt(5.W))
    val wr_data = Input(UInt(32.W))
    val wr_en = Input(UInt(1.W))

    val rd_a = Output(UInt(32.W))
    val rd_b = Output(UInt(32.W))
  })

  val regFile = Mem(32, UInt(32.W))

  when(io.wr_en.toBool()) {
    when(io.wr_addr=/=0.U){
      regFile(io.wr_addr) := io.wr_data
    }

    when(io.rd_addr1===io.wr_addr){
      io.rd_a:=io.wr_data
    }.otherwise({
      io.rd_a:=regFile(io.rd_addr1)
    })
    when(io.rd_addr2===io.wr_addr){
      io.rd_b:=io.wr_data
    }.otherwise({
      io.rd_b:=regFile(io.rd_addr2)
    })
  }.otherwise({
    io.rd_a := regFile(io.rd_addr1)
    io.rd_b := regFile(io.rd_addr2)
  })

  regFile(0):=0.U

  //log

//
//  printf("regfile: rd_addr1=0x%x,rd_a=0x%x,rd_addr2=0x%x,rd_b=0x%x,",
//    io.rd_addr1, io.rd_a, io.rd_addr2, io.rd_b)
////
//  printf("wr_data=0x%x,wr_addr=0x%x,wr_en=0x%x\n",io.wr_data,io.wr_addr,io.wr_en)
  //printf( "regfile: wr_data=0x%x,wr_addr=0x%x,wr_en=0x%x\n",io.wr_data,io.wr_addr,io.wr_en)
}
