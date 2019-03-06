package Pipeline

import Pipeline.consts.ExtOp._
import Pipeline.consts.{CP0Sel, ExcCode, RaSel}
import chisel3._
import chisel3.util._
import consts.ALUctr._



class IDtoEXEIo extends Bundle {
  val ID_RegWr = Output(UInt(2.W))
  val ID_RegSrc = Output(UInt(3.W))

  val ID_MemRd = Output(UInt(1.W))
  val ID_MemWr = Output(UInt(2.W))

  val ID_ALUSrc = Output(UInt(1.W))
  val ID_ALUctr = Output(UInt(6.W))
  val ID_RegDst = Output(UInt(1.W))

  val ID_A = Output(UInt(32.W))
  val ID_B = Output(UInt(32.W))
  val ID_extIm = Output(UInt(32.W))
  val ID_Rs = Output(UInt(5.W))
  val ID_Rt = Output(UInt(5.W))
  val ID_Rd = Output(UInt(5.W))

  val ID_epc = Output(UInt(32.W))
  val ID_BD = Output(UInt(1.W))
  val ID_pc_br = Output(UInt(32.W))
  val ID_pc_jump = Output(UInt(32.W))
  val ID_isEret = Output(UInt(1.W))

}

class IDtoCP0 extends Bundle{
  val raddr = Output(UInt(5.W))
  val rdata = Input(UInt(32.W))
}

class IDtoHazardIo extends Bundle{
  //来自冒险检测单元
  val zero_sel = Input(UInt(1.W))
  val IDwr = Input(UInt(1.W))
  //传给冒险检测单元
  val Rs = Output(UInt(5.W))
  val Rt = Output(UInt(5.W))
}

class IDstageIo extends Bundle {


  //from IF stage
  val ID_IF = Flipped(new IFtoIDIo)

  //ExceptionHandler
  val ID_EXC = Flipped(new ExceptionToStages)

  //to EXE stage
  val ID_EXE = new IDtoEXEIo

  val ID_Hazard = new IDtoHazardIo

  val ID_CP0 = new IDtoCP0


  //  from MEM stage
  val MEM_dst = Input(UInt(5.W))
  val MEM_wbData = Input(UInt(32.W))
  val MEM_RegWr = Input(UInt(2.W))

  //from control
}


class IDstage extends Module {
  val io = IO(new IDstageIo)

  val regFile = Module(new RegFile)
  val decoder = Module(new Decoder)

  //val Op = Wire(UInt(6.W))

  val Imm = Wire(UInt(16.W))
  val Jump = Wire(UInt(26.W))




  val ctr_signal = Wire(UInt(16.W))
  val isEret = WireInit(0.U(1.W))

  val RegWr = WireInit(0.U(2.W))
  val RegSrc = WireInit(0.U(3.W))

  val MemRd = WireInit(0.U(1.W))
  val MemWr = WireInit(0.U(2.W))

  val ALUSrc = WireInit(0.U(1.W))
  val ALUctr = WireInit(0.U(6.W))
  val RegDst = WireInit(0.U(1.W))

  val epc = WireInit(0.U(32.W))
  val BD = WireInit(0.U(1.W))
  val pc_br = Wire(UInt(32.W))
  val pc_jump = Wire(UInt(32.W))

  val Rd_A = Wire(UInt(32.W))
  val Rd_B = Wire(UInt(32.W))
  val extOut = Wire(UInt(32.W))
  val Rs = Wire(UInt(5.W))
  val Rt = Wire(UInt(5.W))
  val Rd = Wire(UInt(5.W))


  val ID_RegWr  = RegEnable(RegWr, 0.U, io.ID_Hazard.IDwr.toBool()  || io.ID_EXC.flush.toBool())
  val ID_RegSrc = RegEnable(RegSrc, 0.U, io.ID_Hazard.IDwr.toBool() || io.ID_EXC.flush.toBool())
  val ID_MemRd  = RegEnable(MemRd, 0.U, io.ID_Hazard.IDwr.toBool()  || io.ID_EXC.flush.toBool())
  val ID_MemWr  = RegEnable(MemWr, 0.U, io.ID_Hazard.IDwr.toBool()  || io.ID_EXC.flush.toBool())
  val ID_ALUSrc = RegEnable(ALUSrc, 0.U, io.ID_Hazard.IDwr.toBool() || io.ID_EXC.flush.toBool())
  val ID_ALUctr = RegEnable(ALUctr, 0.U, io.ID_Hazard.IDwr.toBool() || io.ID_EXC.flush.toBool())
  val ID_RegDst = RegEnable(RegDst, 0.U, io.ID_Hazard.IDwr.toBool() || io.ID_EXC.flush.toBool())
  val ID_epc    = RegEnable(epc,0.U,io.ID_Hazard.IDwr.toBool()      || io.ID_EXC.flush.toBool())
  val ID_BD     = RegEnable(BD , 0.U,io.ID_Hazard.IDwr.toBool()      || io.ID_EXC.flush.toBool())
  val ID_pc_br  = RegEnable(pc_br,0.U,io.ID_Hazard.IDwr.toBool()    || io.ID_EXC.flush.toBool())
  val ID_pc_jump= RegEnable(pc_jump,0.U,io.ID_Hazard.IDwr.toBool()  || io.ID_EXC.flush.toBool())
  val ID_A      = RegEnable(Rd_A, 0.U, io.ID_Hazard.IDwr.toBool()   || io.ID_EXC.flush.toBool())
  val ID_B      = RegEnable(Rd_B, 0.U, io.ID_Hazard.IDwr.toBool()   || io.ID_EXC.flush.toBool())
  val ID_extIm  = RegEnable(extOut,0.U,io.ID_Hazard.IDwr.toBool()   || io.ID_EXC.flush.toBool())
  val ID_Rs     = RegEnable(Rs, 0.U, io.ID_Hazard.IDwr.toBool()     || io.ID_EXC.flush.toBool())
  val ID_Rt     = RegEnable(Rt, 0.U, io.ID_Hazard.IDwr.toBool()     || io.ID_EXC.flush.toBool())
  val ID_Rd     = RegEnable(Rd, 0.U, io.ID_Hazard.IDwr.toBool()     || io.ID_EXC.flush.toBool())
  val ID_isEret = RegEnable(isEret,0.U,io.ID_Hazard.IDwr.toBool()     || io.ID_EXC.flush.toBool())

  when(io.ID_IF.IF_inst===Instructions.ERET){
    isEret:=1.U
  }.otherwise({
    isEret:=0.U
  })

  //Op := io.ID_IF.IF_inst(31, 26)

  Imm :=  io.ID_IF.IF_inst(15, 0)
  Jump := io.ID_IF.IF_inst(25, 0)

  io.ID_Hazard.Rs:=Rs
  io.ID_Hazard.Rt:=Rt

  decoder.io.inst := io.ID_IF.IF_inst




  regFile.io.rd_addr1 := Rs
  regFile.io.rd_addr2 := Rt
  //regFile.io.wr_en := io.MEM_RegWr
  when(io.MEM_RegWr===consts.RegWr.regfile){
    regFile.io.wr_en:=1.U
  }.otherwise({
    regFile.io.wr_en:=0.U
  })
  regFile.io.wr_addr := io.MEM_dst
  regFile.io.wr_data := io.MEM_wbData

  io.ID_CP0.raddr:= Mux(isEret.toBool(),14.U(5.W),io.ID_IF.IF_inst(15,11))
  //io.ID_CP0.raddr:= Rd

  ctr_signal := Mux(io.ID_Hazard.zero_sel.toBool(), 0.U,decoder.io.ctl_signal)


  epc := io.ID_IF.IF_epc
  BD  := io.ID_IF.IF_BD
  pc_jump := Mux(isEret.toBool(),Rd_A,util.Cat(io.ID_IF.IF_pcplus4(31, 28), Jump << 2))
  pc_br :=   io.ID_IF.IF_pcplus4 + (extOut << 2) (31, 0)


  extOut :=MuxCase(sign_ext, Array(
    (decoder.io.extOp === sign_ext) -> Cat(Fill(16, Imm(15)), Imm),
    (decoder.io.extOp === usign_ext) -> Cat(Fill(16, 0.U), Imm)
  ))
  Rs :=   Mux(isEret.toBool(),14.U,Mux(decoder.io.Cp0Sel===CP0Sel.CP0,io.ID_IF.IF_inst(15, 11),io.ID_IF.IF_inst(25, 21)))
  Rt :=   Mux(decoder.io.RaSel===RaSel.Ra,0.U(5.W),io.ID_IF.IF_inst(20, 16))
  Rd :=   Mux(decoder.io.RaSel===RaSel.Ra,31.U(5.W),io.ID_IF.IF_inst(15, 11))


  when(io.ID_EXC.flush.toBool()){
    //exception flush
    RegWr:=0.U
    RegSrc:=0.U

    MemRd:=0.U
    MemWr:=0.U

    ALUSrc:=0.U
    ALUctr:=0.U
    RegDst:=0.U

    Rd_A:=0.U
    Rd_B:=0.U

  }.otherwise({

    RegWr:= ctr_signal(15,14)
    RegSrc:= ctr_signal(13,11)

    MemRd:=ctr_signal(10)
    MemWr:=ctr_signal(9,8)

    ALUSrc:=ctr_signal(7)
    ALUctr:=ctr_signal(6,1)
    RegDst:=ctr_signal(0)

    Rd_A := Mux(decoder.io.Cp0Sel===CP0Sel.CP0,io.ID_CP0.rdata,regFile.io.rd_a)
    Rd_B := Mux(decoder.io.RaSel===RaSel.Ra,io.ID_IF.IF_pcplus4,regFile.io.rd_b)

  })






  io.ID_EXE.ID_RegWr  :=  ID_RegWr
  io.ID_EXE.ID_RegSrc :=  ID_RegSrc

  io.ID_EXE.ID_MemRd  :=  ID_MemRd
  io.ID_EXE.ID_MemWr  :=  ID_MemWr

  io.ID_EXE.ID_ALUSrc :=  ID_ALUSrc
  io.ID_EXE.ID_ALUctr  :=  ID_ALUctr
  io.ID_EXE.ID_RegDst :=  ID_RegDst

  io.ID_EXE.ID_A      :=  ID_A
  io.ID_EXE.ID_B      :=  ID_B
  io.ID_EXE.ID_extIm  :=  ID_extIm
  io.ID_EXE.ID_Rs     :=  ID_Rs
  io.ID_EXE.ID_Rt     :=  ID_Rt
  io.ID_EXE.ID_Rd     :=  ID_Rd

  io.ID_EXE.ID_epc:=ID_epc
  io.ID_EXE.ID_BD:=ID_BD
  io.ID_EXE.ID_pc_br:=ID_pc_br
  io.ID_EXE.ID_pc_jump:= ID_pc_jump
  io.ID_EXE.ID_isEret:=ID_isEret

  when( ctr_signal(6,1)===ctr_beq||
        ctr_signal(6,1)===ctr_bgez||
        ctr_signal(6,1)===ctr_bgtz||
        ctr_signal(6,1)===ctr_blez||
        ctr_signal(6,1)===ctr_bne||
        ctr_signal(6,1)===ctr_bgezal||
        ctr_signal(6,1)===ctr_bltzal||
        ctr_signal(6,1)===ctr_bltz||
        ctr_signal(6,1)===ctr_jal||
        ctr_signal(6,1)===ctr_jalr||
        ctr_signal(6,1)===ctr_jr||
        ctr_signal(6,1)===ctr_jump){

     io.ID_IF.ID_isBr_Jp := 1.U

  }.otherwise({
     io.ID_IF.ID_isBr_Jp := 0.U
  })

  io.ID_EXC.epc:=io.ID_IF.IF_epc
  io.ID_EXC.BD := io.ID_IF.IF_BD

  when(ctr_signal(6,1)===ctr_syscall){

    io.ID_EXC.ExcCode:=ExcCode.Sys
    io.ID_EXC.ExcHappen:=1.U

  }.elsewhen(ctr_signal(6,1)===ctr_break){

    io.ID_EXC.ExcCode:=ExcCode.Bp
    io.ID_EXC.ExcHappen:=1.U

  }.elsewhen(ctr_signal(6,1)===63.U){
    io.ID_EXC.ExcCode:=ExcCode.RI
    io.ID_EXC.ExcHappen:=1.U
  }.otherwise({

    io.ID_EXC.ExcCode:=0.U
    io.ID_EXC.ExcHappen:=0.U

  })

//  printf("ID: Rs=0x%x,RT=0x%x,Rd=0x%x,ALUctr=0x%x,regsrc=0x%x\n",Rs,Rt,Rd,ctr_signal(6,1),ctr_signal(13,11))
//  printf("ID: inst=0x%x,IDwr=0x%x,Rd_b=0x%x\n",io.ID_IF.IF_inst,io.ID_Hazard.IDwr,Rd_B)\
//  printf("ID: epc=0x%x,isEret=0x%x,pc_jp=0x%x,rd_a=0x%x\n",epc,isEret,pc_jump,Rd_A)
}

