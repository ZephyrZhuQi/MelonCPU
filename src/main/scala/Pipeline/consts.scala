package Pipeline

import chisel3._



object consts {
  def enable = 1.U
  def disable= 0.U
  def x = 0.U

  object RegWr{
    def disable = 0.U(2.W)
    def regfile = 1.U(2.W)
    def CP0 = 2.U(2.W)
  }

  object PCsrc{
    def sel_br = "b00".U(2.W)
    def sel_jump = "b01".U(2.W)
    def sel_pcplus4= "b10".U(2.W)
  }
  object ExtOp{
    def sign_ext = "b0".U(1.W)
    def usign_ext = "b1".U(1.W)
  }
  object Cmp{
    def Bigger = "b00".U(2.W)
    def Smaller = "b01".U(2.W)
    def Equal = "b10".U(2.W)
  }
  object RegSrc{
    def MEM_data_W = "b000".U(3.W)
    def MEM_data_B = "b001".U(3.W)
    def MEM_data_BU ="b010".U(3.W)
    def MEM_data_H= "b011".U(3.W)
    def MEM_data_HU ="b100".U(3.W)
    def MEM_ALUout = "b101".U(3.W)
  }

  object ALUOp{
    def Rtype = "b00".U(2.W)
    def Itype = "b01".U(2.W)
    def Jtype = "b10".U(2.W)
  }
  object ALUctr{


    val ctr_add  = "b000000".U(6.W)
    val ctr_sub  = "b000001".U(6.W)
    val ctr_mul  = "b000010".U(6.W)
    val ctr_mulu = "b000011".U(6.W)
    val ctr_div  = "b000100".U(6.W)
    val ctr_divu = "b000101".U(6.W)
    val ctr_slt  = "b011001".U(6.W)
    val ctr_sltu = "b011010".U(6.W)
    val ctr_sllv = "b011011".U(6.W)
    val ctr_srav = "b011100".U(6.W)
    val ctr_srlv = "b011101".U(6.W)



    val ctr_and  = "b000110".U(6.W)
    val ctr_or   = "b000111".U(6.W)
    val ctr_nor  = "b001000".U(6.W)
    val ctr_xor  = "b001001".U(6.W)
    val ctr_sll  = "b001010".U(6.W)
    val ctr_sra  = "b001011".U(6.W)
    val ctr_srl  = "b001100".U(6.W)
    val ctr_lui  = "b001101".U(6.W)


    val ctr_bne  = "b001110".U(6.W)
    val ctr_beq  = "b001111".U(6.W)
    val ctr_bgtz = "b010001".U(6.W)
    val ctr_bgez = "b010000".U(6.W)
    val ctr_blez = "b010010".U(6.W)
    val ctr_bltz = "b010011".U(6.W)
    val ctr_bgezal="b010101".U(6.W)
    val ctr_bltzal="b010100".U(6.W)
    //val ctr_bltzal="b10100".U(5.W)



    val ctr_jump = "b010111".U(6.W)
    val ctr_jal  = "b011000".U(6.W)
    val ctr_jr   = "b100000".U(6.W)
    val ctr_jalr = "b100001".U(6.W)



    val ctr_mfhi = "b100010".U(6.W)
    val ctr_mflo = "b100011".U(6.W)
    val ctr_mthi = "b100100".U(6.W)
    val ctr_mtlo = "b100101".U(6.W)

    val ctr_mtc0 = "b100110".U(6.W)
    val ctr_mfc0 = "b100111".U(6.W)

    val ctr_break ="b101000".U(6.W)
    val ctr_syscall="b101001".U(6.W)

    val ctr_addu = "b101010".U(6.W)
    val ctr_subu = "b101011".U(6.W)

  }
  object ABsrc{
    def A = "b00".U(2.W)
    def B = "b11".U(2.W)
    def DataEx = "b01".U(2.W)
    def DataMem = "b10".U(2.W)
  }
  //b1,b2
  object ALUsrc{
    def B1 = "b0".U(1.W)
    def B2 = "b1".U(1.W)
  }
  object RegDst{
    def Rt = "b0".U(1.W)
    def Rd = "b1".U(1.W)
  }

  object RaSel{
    def Ra = "b0".U(1.W)
    def Rd = "b1".U(1.W)
  }

  object CP0Sel{
    def regfile = "b0".U(1.W)
    def CP0 = "b1".U(1.W)
  }

  object MemWr{
    def N = "b00".U(2.W)
    def SB = "b01".U(2.W)
    def SH = "b10".U(2.W)
    def SW = "b11".U(2.W)
  }


  object ExcCode{
    def Int = "x00".U(5.W)
    def AdEL = "x04".U(5.W)
    def AdES = "x05".U(5.W)
    def Sys = "x08".U(5.W)
    def Bp = "x09".U(5.W)
    def RI = "x0a".U(5.W)
    def Ov = "x0c".U(5.W)
  }



}
