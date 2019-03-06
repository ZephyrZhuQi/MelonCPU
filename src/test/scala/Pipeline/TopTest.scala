package Pipeline

//**************************************************************************
//--------------------------------------------------------------------------
//
// version 0.1
//--------------------------------------------------------------------------
//**************************************************************************

import java.io.File

import Top.Top
import chisel3.iotesters._

import scala.io.Source
import scala.util.control.Breaks._

class TopTest(c: Top) extends PeekPokeTester(c) {

  var last_inst:BigInt=0

  def asUInt(InInt: Int) = (BigInt(InInt >>> 1) << 1) + (InInt & 1)

  def WriteImem(filename:String): Unit ={
    val base_addr = 0
    var addr = 0
    var Inst = 0
    for (line <- Source.fromFile(filename).getLines){
      Inst = Integer.parseUnsignedInt(line, 16)
      poke(c.io.test_wr, 1)
      poke(c.io.test_addr, (addr*4 + base_addr)>>2)
      poke(c.io.test_inst, asUInt(Inst))
      addr = addr + 1
      step(1)
    }
  }

  def WriteImem(filename:File): Unit ={
    val base_addr = 0
    var addr = 0
    var Inst = 0
    for (line <- Source.fromFile(filename).getLines){
      Inst = Integer.parseUnsignedInt(line, 16)
      poke(c.io.test_wr, 1)
      poke(c.io.test_addr, (addr*4 + base_addr)>>2)
      poke(c.io.test_inst, asUInt(Inst))
      addr = addr + 1
      step(1)
    }
  }

  def Run(filename:String,cycle:Int): Unit ={
    WriteImem(filename)
    for (i <- 0 to cycle){
      println("cycle "+i+":")
      poke(c.io.test_wr, 0)
      poke(c.io.test_addr, 0)
      poke(c.io.test_inst, 0)
      step(1)
      last_inst= peek(c.io.MEM_WB_inst)

      if (last_inst == 0x26730001 ){
        println("\nsuccess\n")
        break()
      }
      if (peek(c.io.MEM_WB_inst)==0x00934025){
        println("\nfailed\n")
        break()
      }
    }
  }


  //Run("./InstFiles/decodetest.s",200)
  //Run("./InstFiles/Test/n1_add.s",3000)
  //Run("./InstFiles/Test/n3_addu.s",3000)
  //Run("./InstFiles/Test/n4_addiu.s",10000)
  //Run("./InstFiles/Test/n7_slt.s",25000)
  //Run("./InstFiles/Test/n8_slti.s",25000)
  //Run("./InstFiles/Test/n17_lui.s",25000)
  //Run("./InstFiles/Test/n13_mult.s",25000)
  //Run("./InstFiles/Test/n20_ori.s",25000)
  //Run("./InstFiles/Test/n26_srav.s",25000)
  //Run("./InstFiles/Test/n29_beq.s",25000)
  //Run("./InstFiles/Test/n30_bne.s",25000)
  //Run("./InstFiles/Test/n30_bne.s",25000)
  //Run("./InstFiles/Test/n31_bgez.s",25000)
  //Run("./InstFiles/Test/n32_bgtz.s",25000)
  //Run("./InstFiles/Test/n33_blez.s",25000)
  //Run("./InstFiles/Test/n34_bltz.s",25000)
  //Run("./InstFiles/Test/n35_bltzal.s",25000)
  //Run("./InstFiles/Test/n36_bgezal.s",25000)
  //Run("./InstFiles/Test/n39_jr.s",25000)
  //Run("./InstFiles/Test/n40_jalr.s",25000)
  //Run("./InstFiles/Test/n38_jal.s",25000)
  //Run("./InstFiles/Test/n37_j.s",25000)
  //Run("./InstFiles/Test/n51_lw.s",10000)
  //Run("./InstFiles/Test/n49_lh.s",10000)
  //Run("./InstFiles/Test/n50_lhu.s",10000)
  //Run("./InstFiles/Test/n47_lb.s",10000)
  //Run("./InstFiles/Test/n48_lbu.s",10000)
  //Run("./InstFiles/Test/n54_sw.s",25000)
  //Run("./InstFiles/Test/n52_sb.s",25000)
  //Run("./InstFiles/Test/n53_sh.s",25000)
  //Run("./InstFiles/Test/n52_sb.s",25000)
  //Run("./InstFiles/Test/n41_mfhi.s",25000)
  //Run("InstFiles/Test/n28_srlv.s",30000)
  //Run("./InstFiles/Test/n12_divu.s",30000)
  //Run("./InstFiles/Test/n57_mtc0.s",30000)
  //Run("./InstFiles/Test/n56_mfc0.s",30000)
  //Run("./InstFiles/ttt.s",50)
  //Run("./InstFiles/Test/n64_sh_ex.s",30000)
  //Run("./InstFiles/Test/n58_add_ex.s",30000)
  //Run("./InstFiles/Test/n60_sub_ex.s",30000)
  //Run("./InstFiles/Test/n55_eret.s",30000)
  //Run("./InstFiles/Test/n46_syscall.s",30000)
  Run("./InstFiles/Test/n80_beq_ex_ds.s",30000)
  //Run("./InstFiles/Test/n67_reserved_instruction_ex.s",30000)
  //Run("./InstFiles/Test/n64_sh_ex.s",30000)
  //Run("./InstFiles/Test/n66_eret_ex.s",30000)


}


object TopTest{
  def main (args: Array[String] ): Unit = {
    Driver(()=>new Top,"verilator"){
      (c)=>new TopTest(c)
    }
  }
}
