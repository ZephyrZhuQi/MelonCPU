package Pipeline

import Pipeline.Instructions._
import Pipeline.consts._
import consts.RegSrc._
import ALUsrc._
import ALUctr._
import ExtOp._
import chisel3._

class DecoderIo extends Bundle{
  val inst = Input(UInt(32.W))
  val ctl_signal = Output(UInt(16.W))
  val extOp = Output(UInt(1.W))
  val RaSel = Output(UInt(1.W))
  val Cp0Sel = Output(UInt(1.W))
}

class Decoder extends Module{
  val io = IO(new DecoderIo)

  val s = util.ListLookup(io.inst,

    //                                                    ALUSrc         RegDst
    //                 RegWr            RegSrc      MemRd    MemWr        ALUctr          ExtOp
    List(             0.U(2.W),0.U(3.W),0.U(1.W),0.U(2.W),0.U(1.W),63.U(6.W),0.U(1.W),0.U(1.W),0.U(1.W),0.U(1.W)),Array(

      NOP    -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x,       x         ,   x        ,   x        ,   x       ,   CP0Sel.regfile),
      ADD    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_add   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      ADDI   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_add   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      ADDU   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_addu   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      ADDIU  -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_addu   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      SUB    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_sub   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      SUBU   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_subu   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      MULT   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   B1,      ctr_mul   ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      MULTU  -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   B1,      ctr_mulu  ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      DIVU   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   B1,      ctr_divu  ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      DIV    -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   B1,      ctr_div   ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      SLT    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_slt   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      SLTI   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_slt   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      SLTU   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_sltu  ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      SLTIU  -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_sltu  ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      AND    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_and   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      ANDI   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_and   ,   RegDst.Rt,   usign_ext,   RaSel.Rd,   CP0Sel.regfile),
      LUI    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_lui   ,   RegDst.Rt,   usign_ext,   RaSel.Rd,   CP0Sel.regfile),
      NOR    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_nor   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      OR     -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_or    ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      ORI    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_or    ,   RegDst.Rt,   usign_ext,   RaSel.Rd,   CP0Sel.regfile),
      XOR    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_xor   ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      XORI   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B2,      ctr_xor   ,   RegDst.Rt,   usign_ext,   RaSel.Rd,   CP0Sel.regfile),
      SLLV   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_sllv  ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      SLL    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_sll   ,   RegDst.Rd,   usign_ext,   RaSel.Rd,   CP0Sel.regfile),
      SRAV   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_srav  ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      SRA    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_sra   ,   RegDst.Rd,   usign_ext,   RaSel.Rd,   CP0Sel.regfile),
      SRLV   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_srlv  ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      SRL    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_srl   ,   RegDst.Rd,   usign_ext,   RaSel.Rd,   CP0Sel.regfile),
      BEQ    -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   B1,      ctr_beq   ,   x,           sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      BNE    -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   B1,      ctr_bne   ,   x,           sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      BGEZ   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_bgez  ,   x,           sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      BGTZ   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_bgtz  ,   x,           sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      BLEZ   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_blez  ,   x,           sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      BLTZ   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_bltz  ,   x,           sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      BLTZAL -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   x ,      ctr_bltzal,   RegDst.Rd,   sign_ext ,   RaSel.Ra,   CP0Sel.regfile),
      BGEZAL -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   x ,      ctr_bgezal,   RegDst.Rd,   sign_ext ,   RaSel.Ra,   CP0Sel.regfile),
      J      -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_jump  ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      JAL    -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_jal   ,   RegDst.Rd,   x        ,   RaSel.Ra,   CP0Sel.regfile),
      JR     -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x,       ctr_jr    ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      JALR   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_jalr  ,   RegDst.Rd,   x        ,   RaSel.Ra,   CP0Sel.regfile),
      MTHI   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_mthi  ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      MTLO   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_mtlo  ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      MFHI   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   x ,      ctr_mfhi  ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      MFLO   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   x ,      ctr_mflo  ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      LB     -> List( RegWr.regfile,   MEM_data_B ,   enable ,   MemWr.N ,   B2,      ctr_add   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      LBU    -> List( RegWr.regfile,   MEM_data_BU,   enable ,   MemWr.N ,   B2,      ctr_add   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      LH     -> List( RegWr.regfile,   MEM_data_H ,   enable ,   MemWr.N ,   B2,      ctr_add   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      LHU    -> List( RegWr.regfile,   MEM_data_HU,   enable ,   MemWr.N ,   B2,      ctr_add   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      LW     -> List( RegWr.regfile,   MEM_data_W ,   enable ,   MemWr.N ,   B2,      ctr_add   ,   RegDst.Rt,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      SB     -> List( RegWr.disable,   x          ,   disable,   MemWr.SB,   B2,      ctr_addu   ,   x        ,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      SH     -> List( RegWr.disable,   x          ,   disable,   MemWr.SH,   B2,      ctr_add   ,   x        ,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      SW     -> List( RegWr.disable,   x          ,   disable,   MemWr.SW,   B2,      ctr_add   ,   x        ,   sign_ext ,   RaSel.Rd,   CP0Sel.regfile),
      MTC0   -> List( RegWr.CP0    ,   MEM_ALUout ,   disable,   MemWr.N ,   B1,      ctr_mtc0  ,   RegDst.Rd,   x        ,   RaSel.Rd,   CP0Sel.regfile),
      MFC0   -> List( RegWr.regfile,   MEM_ALUout ,   disable,   MemWr.N ,   x ,      ctr_mfc0  ,   RegDst.Rt,   x        ,   RaSel.Rd,   CP0Sel.CP0    ),
      ERET   -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_jump  ,   x        ,   x        ,   RaSel.Rd,   CP0Sel.CP0    ),
      BREAK  -> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_break ,   x        ,   x        ,   RaSel.Rd,    x            ),
      SYSCALL-> List( RegWr.disable,   x          ,   disable,   MemWr.N ,   x ,      ctr_syscall,  x        ,   x        ,   RaSel.Rd,    x            )

      //      LW     -> List( enable , MEM_data_W, enable , disable, B2, ctr_add   , RegDst.Rt, sign_ext , RaSel.Rd),
      //
      //      SW     -> List( disable, x         , disable, enable , B2, ctr_add   , x        , sign_ext , RaSel.Rd)
    ))

  val (regWr:UInt)::(regSrc:UInt)::(memRd:UInt)::(memWr:UInt)::(aluSrc:UInt)::(aluCtr:UInt)::(regDst:UInt)::(extOp:UInt)::(raSel:UInt)::(cp0Sel:UInt)::Nil = s

  io.ctl_signal:=util.Cat(regWr,regSrc,memRd,memWr,aluSrc,aluCtr,regDst)
  io.extOp:=extOp
  io.RaSel:=raSel
  io.Cp0Sel:=cp0Sel


}