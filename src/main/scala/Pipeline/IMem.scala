package Pipeline

import chisel3._


class IMemIo extends Bundle{
  val Imem_IF = Flipped(new IFtoImemIo)

  //test IO
  val test_wr = Input(UInt(1.W))
  val test_addr = Input(UInt(32.W))
  val test_inst = Input(UInt(32.W))
}

class IMem extends Module{
  val io = IO(new IMemIo)

  val imem = Mem(30000,UInt(32.W))

  io.Imem_IF.rd_data := 0.U

  //test mode
  //load instructions into imem
  when(io.test_wr.toBool()){

    imem(io.test_addr):=io.test_inst

  }.otherwise({

    //IFetch mode
    when(io.Imem_IF.wr.toBool()){
      imem(io.Imem_IF.addr) := io.Imem_IF.wr_data
    }.otherwise({
      io.Imem_IF.rd_data  := imem(io.Imem_IF.addr)
    })

  })


  //在装载指令时显示log
/*
  printf("addr=0x%x,data=0x%x,test_addr=0x%x,test_data=0x%x\n",
    io.Imem_IF.addr,   imem(io.Imem_IF.addr),
    io.test_addr,       imem(io.test_addr))
*/


}

/*
object IMem extends App {
  Driver.execute(args,()=>new IMem)
}
*/
