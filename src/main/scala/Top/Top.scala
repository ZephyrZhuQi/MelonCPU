package Top

import Pipeline._
import Pipeline.consts.RegWr
import chisel3._

class TestIo extends Bundle {

  val test_wr = Input(UInt(1.W))
  val test_addr = Input(UInt(32.W))
  val test_inst = Input(UInt(32.W))

  val MEM_WB_inst = Output(UInt(32.W))

}

class Top extends Module {
  val io = IO(new TestIo)
  //stage 1
  val imem = Module(new IMem)
  val IF = Module(new IFstage)

  io.MEM_WB_inst:=IF.io.IF_ID.IF_inst

  //stage 2
  //val regFile = Module(new RegFile)
  val ID = Module(new IDstage)
  val hazardUnit = Module(new HazardUnit)

  val control = Module(new Control)
  //stage3
  val bypassUnit = Module(new BypassUnit)
  val EXE = Module(new EXstage)
  //stage 4
  //val dmem = Module(new DMem)
  val MEM = Module(new MEMstage)
  //stage 5
  val WB = Module(new WBstage)

  val cp0 = Module(new CP0.CP0)
  val ExcHandler = Module(new ExceptionHandler)


  //connect hardwares
  IF.io.IF_Imem<>imem.io.Imem_IF
  IF.io.IFwr:=hazardUnit.io.IFwr
  IF.io.PCwr:=hazardUnit.io.PCwr

  IF.io.IF_flush:=control.io.IF_flush
  IF.io.PCsrc:=control.io.PCSrc

  IF.io.pc_br:=EXE.io.pc_br
  IF.io.pc_jump:=EXE.io.pc_jump

  IF.io.IF_ID<>ID.io.ID_IF


  ID.io.ID_Hazard<>hazardUnit.io.Hazard_ID

  ID.io.MEM_wbData:=WB.io.MEM_wbData
  ID.io.MEM_dst:=MEM.io.MEM_dst
  ID.io.MEM_RegWr:=MEM.io.MEM_RegWr

  ID.io.ID_EXE<>EXE.io.EX_ID

  ID.io.ID_CP0.rdata:=cp0.io.out

  EXE.io.Asrc:=bypassUnit.io.Asrc
  EXE.io.Bsrc:=bypassUnit.io.Bsrc
  EXE.io.MEM_bypass_data:=WB.io.MEM_wbData

  EXE.io.EX_MEM<>MEM.io.MEM_EXE
  EXE.io.isJR := control.io.isJR

  control.io.cond:=EXE.io.cond
  control.io.ALUctr:=EXE.io.ALUctr

  MEM.io.MEM_WB<>WB.io.WB_MEM




  hazardUnit.io.ID_MemRd:=EXE.io.ID_MemRd
  hazardUnit.io.ID_Rt:=EXE.io.Rt
  hazardUnit.io.ALUctr:=EXE.io.ALUctr
  hazardUnit.io.complete:=EXE.io.complete

  bypassUnit.io.ID_Rs:=EXE.io.Rs
  bypassUnit.io.ID_Rt:=EXE.io.Rt
  bypassUnit.io.EX_dst:=EXE.io.EX_dst
  bypassUnit.io.EX_RegWr:=EXE.io.EX_RegWr
  bypassUnit.io.MEM_dst:=MEM.io.MEM_dst
  bypassUnit.io.MEM_RegWr:=MEM.io.MEM_RegWr

  IF.io.test_wr:=io.test_wr
  imem.io.test_wr := io.test_wr
  imem.io.test_addr := io.test_addr
  imem.io.test_inst := io.test_inst





  when(MEM.io.MEM_RegWr===RegWr.CP0){
    cp0.io.en_0:=1.U
    cp0.io.waddr_0:=MEM.io.MEM_dst
    cp0.io.wdata_0:=WB.io.MEM_wbData
  }.otherwise({
    cp0.io.en_0:=0.U
    cp0.io.waddr_0:=0.U
    cp0.io.wdata_0:=0.U
  })

  cp0.io.raddr:=ID.io.ID_CP0.raddr

  ExcHandler.io.Exc_CP0<>cp0.io.CP0_Exc
  ExcHandler.io.Exc_IF<>IF.io.IF_Exc
  ExcHandler.io.Exc_ID<>ID.io.ID_EXC
  ExcHandler.io.Exc_EXE<>EXE.io.EX_Exc
  ExcHandler.io.Exc_MEM<>MEM.io.MEM_Exc


  //printf("Top: cp0wdata=0x%x,addr=0x%x,en=0x%x\n",cp0.io.wdata_0,cp0.io.waddr_0,cp0.io.en_0)

}
object TopDriver extends App {
  chisel3.Driver.execute(args, () => new Top)
}
