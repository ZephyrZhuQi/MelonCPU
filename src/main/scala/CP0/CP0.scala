package CP0
import chisel3._
import Pipeline.ExceptionToCP0
import Pipeline.consts.ExcCode
class CP0 extends Module{
  val io = IO(new Bundle{
    val CP0_Exc = Flipped(new ExceptionToCP0)

    val en_0 = Input(UInt(1.W))
    val waddr_0 = Input(UInt(5.W))
    val wdata_0 = Input(UInt(32.W))


    val raddr = Input(UInt(5.W))
    val out = Output(UInt(32.W))

  })
  val regs = Mem(32,UInt(32.W))

  when(io.en_0.toBool()){
    regs(io.waddr_0):=io.wdata_0
  }

  when(io.waddr_0===io.raddr){
    io.out:=io.wdata_0
  }.otherwise({
    io.out:=regs(io.raddr)
  })

  when(io.CP0_Exc.wr.toBool()){
    regs(12):=io.CP0_Exc.Status
    regs(13):=io.CP0_Exc.Cause
    regs(14):=io.CP0_Exc.EPC
    when(io.CP0_Exc.Cause(6,2)===ExcCode.AdES || io.CP0_Exc.Cause(6,2)===ExcCode.AdEL ){
      regs(8):=io.CP0_Exc.BadVAddr
    }
  }
//  printf("CP0:epc=0x%x\n",regs(14))


//
//  printf("CP0: wrData=0x%x,wrAddr=0x%x,en=0x%x\n",io.wdata_0,io.waddr_0,io.en_0)
//  printf("CP0: raddr=0x%x,rddata=0x%x\n",io.raddr,io.out)
}
