package Pipeline

import Pipeline.consts.ALUctr
import chisel3._

class HazardUnitIo extends Bundle {

  val Hazard_ID = Flipped(new IDtoHazardIo)

  val ALUctr = Input(UInt(6.W))
  val complete = Input(UInt(1.W))
  val ID_Rt = Input(UInt(5.W))
  val ID_MemRd = Input(UInt(1.W))

  val IFwr = Output(UInt(1.W))
  val PCwr = Output(UInt(1.W))

}

class HazardUnit extends Module {
  val io = IO(new HazardUnitIo)
  //start working!
  io.PCwr := 1.U
  io.IFwr:=1.U
  io.Hazard_ID.IDwr:=1.U
  io.Hazard_ID.zero_sel:=0.U
  //load+R type
  when(io.ID_MemRd.toBool() && (io.ID_Rt === io.Hazard_ID.Rt || io.ID_Rt === io.Hazard_ID.Rs)) {
    io.PCwr := 0.U
    io.IFwr := 0.U
    io.Hazard_ID.IDwr := 1.U
    io.Hazard_ID.zero_sel := 1.U
  }
  when((!io.complete.toBool()) &&
    (io.ALUctr === ALUctr.ctr_mul ||
      io.ALUctr === ALUctr.ctr_div ||
      io.ALUctr === ALUctr.ctr_mulu ||
      io.ALUctr === ALUctr.ctr_divu)
  ){
    //mult/div
    //stall
    io.PCwr := 0.U
    io.IFwr:=0.U
    io.Hazard_ID.IDwr:=0.U
    io.Hazard_ID.zero_sel:=0.U
  }
//  printf("Hazard: aluctr=0x%x,complete=0x%x,IDwr=0x%x\n",io.ALUctr,io.complete,io.Hazard_ID.IDwr)
}
