package Pipeline

import Pipeline.consts.RegSrc
import chisel3._


class MEMtoWBIo extends Bundle {
  val MEM_RegSrc = Output(UInt(3.W))
  val MEM_data = Output(UInt(32.W))
  val MEM_ALUout = Output(UInt(32.W))
  val MEM_epc = Output(UInt(32.W))
  val wbData = Input(UInt(32.W))
}

class MEMIo extends Bundle {
  val MEM_WB = new MEMtoWBIo
  val MEM_EXE = Flipped(new EXtoMEMIo)
  val MEM_Exc = Flipped(new ExceptionToMEM)

  val MEM_RegWr = Output(UInt(2.W))
  val MEM_dst = Output(UInt(5.W))
}

class MEMstage extends Module {
  val io = IO(new MEMIo)
  val dmem = Module(new DMem)

  val load_data = WireInit(0.U(32.W))

  val RegWr = WireInit(0.U(2.W))
  val regSrc = WireInit(0.U(3.W))

  val ALUout = WireInit(0.U(32.W))
  val dst = WireInit(0.U(5.W))
  val epc = WireInit(0.U(32.W))


  val MEM_RegWr = RegNext(RegWr)
  val MEM_RegSrc = RegNext(regSrc)

  val MEM_data = RegNext(load_data)

  val MEM_ALUout = RegNext(ALUout)
  val MEM_dst = RegNext(dst)
  val MEM_epc = RegNext(epc)

  val addr = io.MEM_EXE.EX_ALUout >> 2
  val index_B = io.MEM_EXE.EX_ALUout(1, 0)
  val index_H = io.MEM_EXE.EX_ALUout(1)

  dmem.io.Addr := io.MEM_EXE.EX_ALUout

  dmem.io.Memwr := Mux(io.MEM_Exc.flush.toBool(),consts.MemWr.N, io.MEM_EXE.EX_MemWr)
  dmem.io.MemRd := io.MEM_EXE.EX_MemRd

  ALUout := io.MEM_EXE.EX_ALUout
  epc := io.MEM_EXE.EX_epc

  when(io.MEM_Exc.flush.toBool()) {
    RegWr := 0.U
    regSrc := 0.U
    dst := 0.U

  }.otherwise({
    RegWr := io.MEM_EXE.EX_RegWr
    regSrc := io.MEM_EXE.Ex_RegSrc
    dst := io.MEM_EXE.EX_dst
  })


  when(io.MEM_EXE.Ex_RegSrc === RegSrc.MEM_data_B) {
    when(index_B === "b00".U) {
      load_data := util.Cat(util.Fill(24, dmem.io.out(7)), dmem.io.out(7, 0))
    }.elsewhen(index_B === "b01".U) {
      load_data := util.Cat(util.Fill(24, dmem.io.out(15)), dmem.io.out(15, 8))
    }.elsewhen(index_B === "b10".U) {
      load_data := util.Cat(util.Fill(24, dmem.io.out(23)), dmem.io.out(23, 16))
    }.otherwise({
      load_data := util.Cat(util.Fill(24, dmem.io.out(31)), dmem.io.out(31, 24))
    })
  }.elsewhen(io.MEM_EXE.Ex_RegSrc === RegSrc.MEM_data_BU) {
    when(index_B === "b00".U) {
      load_data := util.Cat(util.Fill(24, 0.U), dmem.io.out(7, 0))
    }.elsewhen(index_B === "b01".U) {
      load_data := util.Cat(util.Fill(24, 0.U), dmem.io.out(15, 8))
    }.elsewhen(index_B === "b10".U) {
      load_data := util.Cat(util.Fill(24, 0.U), dmem.io.out(23, 16))
    }.otherwise({
      load_data := util.Cat(util.Fill(24, 0.U), dmem.io.out(31, 24))
    })

  }.elsewhen(io.MEM_EXE.Ex_RegSrc === RegSrc.MEM_data_H) {

    when(index_H === "b0".U) {
      load_data := util.Cat(util.Fill(16, dmem.io.out(15)), dmem.io.out(15, 0))
    }.otherwise({
      load_data := util.Cat(util.Fill(16, dmem.io.out(31)), dmem.io.out(31, 16))
    })

  }.elsewhen(io.MEM_EXE.Ex_RegSrc === RegSrc.MEM_data_HU) {
    when(index_H === "b0".U) {
      load_data := util.Cat(util.Fill(16, 0.U), dmem.io.out(15, 0))
    }.otherwise({
      load_data := util.Cat(util.Fill(16, 0.U), dmem.io.out(31, 16))
    })
  }.otherwise({
    load_data := dmem.io.out
  })


  when(io.MEM_EXE.EX_dst === MEM_dst && MEM_dst =/= 0.U) {
    dmem.io.wr_data := io.MEM_WB.wbData
  }.otherwise({
    dmem.io.wr_data := io.MEM_EXE.EX_data
  })


  io.MEM_EXE.EX_bypass_data := io.MEM_EXE.EX_ALUout

  io.MEM_RegWr := MEM_RegWr
  io.MEM_dst := MEM_dst

  io.MEM_WB.MEM_RegSrc := MEM_RegSrc
  io.MEM_WB.MEM_data := MEM_data
  io.MEM_WB.MEM_ALUout := MEM_ALUout
  io.MEM_WB.MEM_epc := MEM_epc

  io.MEM_Exc.epc := epc
  io.MEM_Exc.BD := io.MEM_EXE.EX_BD
  io.MEM_Exc.badVaddr:=io.MEM_EXE.EX_ALUout


  when(io.MEM_EXE.EX_MemRd.toBool()) {
    when(io.MEM_EXE.EX_ALUout(1, 0) =/= 0.U(2.W) && io.MEM_EXE.Ex_RegSrc === RegSrc.MEM_data_W ||
      io.MEM_EXE.EX_ALUout(0) =/= 0.U(1.W) && io.MEM_EXE.Ex_RegSrc === RegSrc.MEM_data_H ||
      io.MEM_EXE.EX_ALUout(0) =/= 0.U(1.W) && io.MEM_EXE.Ex_RegSrc === RegSrc.MEM_data_HU) {

      //可能读异常
      io.MEM_Exc.ExcCode := consts.ExcCode.AdEL
      io.MEM_Exc.ExcHappen := 1.U(1.W)

    }.otherwise({
      io.MEM_Exc.ExcCode := 0.U
      io.MEM_Exc.ExcHappen := 0.U
    })
  }.elsewhen((io.MEM_EXE.EX_ALUout(0) =/= 0.U(1.W) && io.MEM_EXE.EX_MemWr === consts.MemWr.SH) ||
    (io.MEM_EXE.EX_ALUout(1, 0) =/= 0.U(2.W) && io.MEM_EXE.EX_MemWr === consts.MemWr.SW)) {
    //可能写异常
    io.MEM_Exc.ExcCode := consts.ExcCode.AdES
    io.MEM_Exc.ExcHappen := 1.U(1.W)


  }.otherwise({
    io.MEM_Exc.ExcCode := 0.U
    io.MEM_Exc.ExcHappen := 0.U
  })


}
