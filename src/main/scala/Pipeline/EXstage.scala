package Pipeline

import Pipeline.consts._
import chisel3._
import chisel3.util.MuxCase


class EXtoMEMIo extends Bundle {
  val EX_bypass_data = Input(UInt(32.W))

  val EX_RegWr = Output(UInt(2.W))
  val Ex_RegSrc = Output(UInt(3.W))

  val EX_MemRd = Output(UInt(1.W))
  val EX_MemWr = Output(UInt(2.W))

  val EX_ALUout = Output(UInt(32.W))
  val EX_data = Output(UInt(32.W))
  val EX_dst = Output(UInt(5.W))

  val EX_epc = Output(UInt(32.W))
  val EX_BD = Output(UInt(1.W))
}

class EXIo extends Bundle {
  val EX_ID = Flipped(new IDtoEXEIo)
  val EX_MEM = new EXtoMEMIo
  val EX_Exc = Flipped(new ExceptionToStages)

  //from wb
  val MEM_bypass_data = Input(UInt(32.W))

  //from bypass unit
  val Asrc = Input(UInt(2.W))
  val Bsrc = Input(UInt(2.W))

  //to bypass unit
  val Rs = Output(UInt(5.W))
  val Rt = Output(UInt(5.W))
  val EX_dst = Output(UInt(5.W))
  val EX_RegWr = Output(UInt(2.W))

  //to Hazard unit
  val ID_MemRd = Output(UInt(1.W))
  val complete = Output(UInt(1.W))
  //
  val ALUctr = Output(UInt(6.W))
  //
  //to Control
  val cond = Output(UInt(1.W))
  val isJR = Input(UInt(1.W))

  //to IF
  val pc_br = Output(UInt(32.W))
  val pc_jump = Output(UInt(32.W))

}


class EXstage extends Module {
  val io = IO(new EXIo)
  val alu = Module(new ALU)

  val B = WireInit(UInt(32.W), 0.U)
  val B1 = WireInit(UInt(32.W), 0.U)
  val B2 = WireInit(UInt(32.W), 0.U)

  val dst = WireInit(UInt(5.W), 0.U)
  val ALUctr = WireInit(UInt(6.W), 0.U)

  val RegWr = WireInit(0.U(2.W))
  val RegSrc = WireInit(0.U(3.W))
  val MemRd = WireInit(0.U(1.W))
  val MemWr = WireInit(0.U(2.W))

  val ALUout = WireInit(0.U(32.W))
  val epc = WireInit(0.U(32.W))
  val BD = WireInit(0.U(1.W))

  val A = WireInit(UInt(32.W), 0.U)
  val A1 = WireInit(UInt(32.W), 0.U) // rs
  val A2 = WireInit(UInt(32.W), 0.U) // sham

  val EX_RegWr = RegNext(RegWr)
  val Ex_RegSrc = RegNext(RegSrc)
  val EX_MemRd = RegNext(MemRd)
  val EX_MemWr = RegNext(MemWr)

  val EX_ALUout = RegNext(ALUout)
  val EX_data = RegNext(B1)
  val EX_dst = RegNext(dst)

  val EX_epc = RegNext(epc)
  val EX_BD = RegNext(BD)


  ALUctr := io.EX_ID.ID_ALUctr


  io.EX_MEM.EX_RegWr := EX_RegWr
  io.EX_MEM.Ex_RegSrc := Ex_RegSrc
  io.EX_MEM.EX_MemRd := EX_MemRd
  io.EX_MEM.EX_MemWr := EX_MemWr
  io.EX_MEM.EX_ALUout := EX_ALUout
  io.EX_MEM.EX_data := EX_data
  io.EX_MEM.EX_dst := EX_dst
  io.EX_MEM.EX_epc := EX_epc
  io.EX_MEM.EX_BD := EX_BD

  io.EX_RegWr := EX_RegWr
  io.EX_dst := EX_dst


  ALUout := alu.io.ALUout
  B1 := MuxCase(io.EX_ID.ID_B, Array(
    (io.Bsrc === ABsrc.B) -> io.EX_ID.ID_B,
    (io.Bsrc === ABsrc.DataEx) -> io.EX_MEM.EX_bypass_data,
    (io.Bsrc === ABsrc.DataMem) -> io.MEM_bypass_data
  ))
  dst := MuxCase(RegDst.Rd, Array(
    (io.EX_ID.ID_RegDst === RegDst.Rt) -> io.EX_ID.ID_Rt,
    (io.EX_ID.ID_RegDst === RegDst.Rd) -> io.EX_ID.ID_Rd
  ))
  epc:=io.EX_ID.ID_epc
  BD := io.EX_ID.ID_BD


  when(io.EX_Exc.flush.toBool()) {

    RegWr  := 0.U
    RegSrc := 0.U
    MemRd :=  0.U
    MemWr :=  0.U

  }.otherwise({

    RegWr:=io.EX_ID.ID_RegWr
    RegSrc := io.EX_ID.ID_RegSrc
    MemRd :=  io.EX_ID.ID_MemRd
    MemWr :=  io.EX_ID.ID_MemWr

  })


  io.ID_MemRd := io.EX_ID.ID_MemRd
  io.complete := alu.io.complete
  io.ALUctr := ALUctr
  io.cond := alu.io.cond

  io.Rs := io.EX_ID.ID_Rs
  io.Rt := io.EX_ID.ID_Rt

  A1 := MuxCase(io.EX_ID.ID_A, Array(
    (io.Asrc === ABsrc.A) -> io.EX_ID.ID_A,
    (io.Asrc === ABsrc.DataEx) -> io.EX_MEM.EX_bypass_data,
    (io.Asrc === ABsrc.DataMem) -> io.MEM_bypass_data
  ))
  A2 := util.Cat(0.U(27.W), io.EX_ID.ID_extIm(10, 6))
  A := MuxCase(A1, Array(
    (ALUctr === consts.ALUctr.ctr_sll) -> A2,
    (ALUctr === consts.ALUctr.ctr_sra) -> A2,
    (ALUctr === consts.ALUctr.ctr_srl) -> A2
  ))

  alu.io.A := A



  B2 := io.EX_ID.ID_extIm

  B := MuxCase(B1, Array(
    (io.EX_ID.ID_ALUSrc === ALUsrc.B1) -> B1,
    (io.EX_ID.ID_ALUSrc === ALUsrc.B2) -> B2
  ))
  alu.io.B := B

  alu.io.ALUctr := ALUctr



  io.Rs := io.EX_ID.ID_Rs
  io.Rt := io.EX_ID.ID_Rt


  io.pc_br := io.EX_ID.ID_pc_br
  //io.pc_jump := Mux(io.isJR.toBool(), A1, io.EX_ID.ID_pc_jump)
  io.pc_jump := Mux(io.isJR.toBool()||io.EX_ID.ID_isEret.toBool(), A1, io.EX_ID.ID_pc_jump)

  io.EX_Exc.ExcCode := ExcCode.Ov
  io.EX_Exc.epc := io.EX_ID.ID_epc
  io.EX_Exc.BD := io.EX_ID.ID_BD

  when(alu.io.exc === 1.U) {

    io.EX_Exc.ExcHappen := 1.U
  }.otherwise({
    io.EX_Exc.ExcHappen := 0.U
  })
//  printf("Exe: A1=0x%x,B=0x%x,jump=0x%x,isEret=0x%x\n",A1,B,io.pc_jump,io.EX_ID.ID_isEret)
  //printf("EXE: ID_Rs=0x%x,ID_Rt=0x%x,ID_Rd=0x%x,ALUctr=0x%x\n",io.EX_ID.ID_Rs,io.EX_ID.ID_Rt,
  // io.EX_ID.ID_Rd, io.EX_ID.ID_ALUctr)
  //printf("EXE: pc+4=0x%x\n",io.EX_ID.ID_epc)
}
