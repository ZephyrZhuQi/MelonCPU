package Pipeline
import chisel3._
import chisel3.util._

class ExceptionToStages extends Bundle{
  val flush = Output(UInt(1.W))

  val BD = Input(UInt(1.W))
  val ExcCode = Input(UInt(5.W))
  val epc = Input(UInt(32.W))
  val ExcHappen = Input(UInt(1.W))
}

class  ExceptionToMEM extends ExceptionToStages{
  val badVaddr = Input(UInt(32.W))
}





class ExceptionToCP0 extends Bundle{
  val wr = Output(UInt(1.W))
  val BadVAddr = Output(UInt(32.W))
  val Status = Output(UInt(32.W))
  val Cause = Output(UInt(32.W))
  val EPC = Output(UInt(32.W))
}

class ExceptionHandler extends Module{
  val io = IO(new Bundle{
    val Exc_IF = new ExceptionToMEM
    val Exc_ID = new ExceptionToStages
    val Exc_EXE = new ExceptionToStages
    val Exc_MEM = new ExceptionToMEM
    val Exc_CP0 = new ExceptionToCP0
  })

  val EXL = WireInit(0.U(1.W))

  val BD = WireInit(0.U(1.W))

  EXL:=1.U
  BD:=0.U

  io.Exc_CP0.Status:=Cat(0.U(16.W),1.U(8.W),0.U(6.W),EXL,1.U(1.W))
  io.Exc_CP0.Cause:=0.U
  io.Exc_CP0.BadVAddr:=0.U
  io.Exc_CP0.EPC:=0.U
  io.Exc_CP0.wr:=0.U

  io.Exc_IF.flush:=0.U
  io.Exc_ID.flush:=0.U
  io.Exc_EXE.flush:=0.U
  io.Exc_MEM.flush:=0.U



  when(io.Exc_MEM.ExcHappen.toBool()){
    //to CP0
    BD := io.Exc_MEM.BD
    io.Exc_CP0.wr:=1.U
    io.Exc_CP0.Cause:=Cat(BD,0.U(1.W),0.U(14.W),0.U(8.W),0.U(1.W),io.Exc_MEM.ExcCode,0.U(2.W))
    io.Exc_CP0.EPC:=io.Exc_MEM.epc
    io.Exc_CP0.BadVAddr:=io.Exc_MEM.badVaddr
    //to pipeline
    io.Exc_IF.flush:=1.U
    io.Exc_ID.flush:=1.U
    io.Exc_EXE.flush:=1.U
    io.Exc_MEM.flush:=1.U
  }.elsewhen(io.Exc_EXE.ExcHappen.toBool()){
    //to CP0
    BD := io.Exc_EXE.BD
    io.Exc_CP0.wr:=1.U
    io.Exc_CP0.Cause:=Cat(BD,0.U(1.W),0.U(14.W),0.U(8.W),0.U(1.W),io.Exc_EXE.ExcCode,0.U(2.W))
    io.Exc_CP0.EPC:=io.Exc_EXE.epc
    io.Exc_CP0.BadVAddr:=0.U
    //to pipeline
    io.Exc_IF.flush:=1.U
    io.Exc_ID.flush:=1.U
    io.Exc_EXE.flush:=1.U
    io.Exc_MEM.flush:=0.U
  }.elsewhen(io.Exc_ID.ExcHappen.toBool()){
    //to CP0
    BD:= io.Exc_ID.BD
    io.Exc_CP0.wr:=1.U
    io.Exc_CP0.Cause:=Cat(BD,0.U(1.W),0.U(14.W),0.U(8.W),0.U(1.W),io.Exc_ID.ExcCode,0.U(2.W))
    io.Exc_CP0.EPC:=io.Exc_ID.epc
    io.Exc_CP0.BadVAddr:=0.U
    //to pipeline
    io.Exc_IF.flush:=1.U
    io.Exc_ID.flush:=1.U
    io.Exc_EXE.flush:=0.U
    io.Exc_MEM.flush:=0.U
  }.elsewhen(io.Exc_IF.ExcHappen.toBool()){
    //to CP0
    BD:=io.Exc_IF.BD
    io.Exc_CP0.wr:=1.U
    io.Exc_CP0.Cause:=Cat(BD,0.U(1.W),0.U(14.W),0.U(8.W),0.U(1.W),io.Exc_IF.ExcCode,0.U(2.W))
    io.Exc_CP0.EPC:=io.Exc_IF.epc
    io.Exc_CP0.BadVAddr:=io.Exc_IF.badVaddr
    //to pipeline
    io.Exc_IF.flush:=1.U
    io.Exc_ID.flush:=0.U
    io.Exc_EXE.flush:=0.U
    io.Exc_MEM.flush:=0.U
  }.otherwise({
    //to CP0
    io.Exc_CP0.wr:=0.U
    io.Exc_CP0.Cause:=0.U
    io.Exc_CP0.EPC:=0.U
    io.Exc_CP0.BadVAddr:=0.U
    //to pipeline
    io.Exc_IF.flush:=0.U
    io.Exc_ID.flush:=0.U
    io.Exc_EXE.flush:=0.U
    io.Exc_MEM.flush:=0.U
  })

//  printf("ExHandler:Cause.BD=0x%x\n",io.Exc_CP0.Cause(31))





}
