package Pipeline

import chisel3._

import chisel3.util._

import consts.ALUctr._

class ALU extends Module {

  val io = IO(new Bundle {

    val A = Input(UInt(32.W))

    val B = Input(UInt(32.W))

    val ALUctr = Input(UInt(6.W))

    val ALUout = Output(UInt(32.W))

    val cond = Output(UInt(1.W))

    val complete = Output(UInt(1.W))

    val exc = Output(UInt(1.W))
  })

  val HiLo = RegInit(init = 0.U(64.W))



  //符号位

  //  val sign_A = io.A(31)

  //  val sign_B = io.B(31)

  //  val sign = sign_A ^ sign_B

  ////

  //逻辑计算

  val and_out = io.A & io.B

  val or_out = io.A | io.B

  val nor_out = ~or_out

  val xor_out = io.A ^ io.B

  //移位

  val shamt = io.A(15, 0)

  val sll_out = io.B << io.A(15, 0)

  val srl_out = io.B >> io.A(15, 0)

  val sra_out = (io.B.asSInt() >> io.A(15, 0)).asUInt() ///算数右移

  //乘除法所需

  val cnt = RegInit(init = 0.U(6.W))

  val result = Reg(UInt(68.W))

  val A = Wire(UInt(34.W))

  val B = Wire(UInt(34.W))

  val A_u = Wire(UInt(34.W))

  val B_u = Wire(UInt(34.W))

  val a_2  = Reg(UInt(34.W))

  val a_2c = Reg(UInt(34.W))

  val a_c  = Reg(UInt(34.W))

  val z    = RegInit(init = 0.U)

  val regA = RegInit(0.U(34.W))

  val regB = RegInit(0.U(34.W))

  //算数计算

  val adder = Module(new Adder(34))

  val s_init::s_ready::s_acc::s_done::s_finish::s_pre::Nil = Enum(6)//乘法状态机

  val state = RegInit(s_init)

  io.complete:=0.U

  adder.io.Cin:=0.U

  adder.io.A:=0.U

  adder.io.B:=0.U

  io.ALUout:=0.U

  io.cond:=0.U
  io.exc := 0.U
  //有符号运算

  A :=  util.Cat(util.Cat(io.A(31),io.A(31)),io.A(31,0))

  B :=  util.Cat(util.Cat(io.B(31),io.B(31)),io.B(31,0))

  //无符号运算

  A_u := util.Cat(0.U(2.W),io.A(31,0))

  B_u := util.Cat(0.U(2.W),io.B(31,0))

  switch(io.ALUctr) {

    is(ctr_add) {

      adder.io.A := A

      adder.io.B := B

      adder.io.Cin := 0.U

      io.ALUout := adder.io.Sum(31,0)
      when(adder.io.Overflow===1.U){
        io.exc := 1.U
      }

    }

    is(ctr_addu){
      adder.io.A := A

      adder.io.B := B

      adder.io.Cin := 0.U

      io.ALUout := adder.io.Sum(31,0)
    }

    is(ctr_sub) {

      adder.io.A := A

      adder.io.B := ~B

      adder.io.Cin := 1.U

      io.ALUout := adder.io.Sum(31,0)
      when(adder.io.Overflow===1.U){
        io.exc := 1.U
      }

    }

    is(ctr_subu){
      adder.io.A := A

      adder.io.B := ~B

      adder.io.Cin := 1.U

      io.ALUout := adder.io.Sum(31,0)
    }


    is(ctr_div) {

      switch(state) {

        is(s_init) {

          HiLo := util.Cat(io.A, io.B)

          adder.io.A := ~util.Cat(util.Fill(2, io.A(31)), io.A)

          adder.io.B := 1.U

          adder.io.Cin := 0.U

          when(io.A(31) === 0.U) {

            regA := util.Cat(0.U(2.W), io.A)

          }.otherwise({

            regA := adder.io.Sum

          })

          state := s_pre

          cnt := 0.U

        }//处理A的符号

        is(s_pre) {

          adder.io.A := ~util.Cat(util.Fill(2, HiLo(31)), HiLo(31, 0))

          adder.io.B := 1.U

          adder.io.Cin := 0.U

          when(HiLo(31) === 0.U) {

            regB := util.Cat(0.U(2.W), HiLo(31, 0))

          }.otherwise({

            regB := adder.io.Sum

          })

          result := util.Cat(0.U(34.W), regA)

          state := s_acc

        }//处理B 的符号

        is(s_acc) {

          adder.io.A := ~regB

          adder.io.B := result(67, 34)

          adder.io.Cin := 1.U

          cnt := cnt + 1.U

          when(adder.io.Sum(33) === 0.U) {

            result := util.Cat(adder.io.Sum(32, 0), result(33, 1), "b10".U(2.W))

          }.otherwise({

            result := util.Cat(result(66, 0), 0.U(1.W))

          })

          when(cnt =/= 33.U) {

            state := s_acc

          }.otherwise({

            state := s_ready

          })

        }

        is(s_ready) {

          adder.io.A := ~regB

          adder.io.B := result(67, 34)

          adder.io.Cin := 1.U

          when(adder.io.Sum(33) === 0.U) {

            result := util.Cat(adder.io.Sum(33, 0), result(33, 1), "b1".U(1.W))

          }.otherwise({

            result := result(67, 0)

          })

          state := s_done

        }

        is(s_done) {

          when(result(67,34)===0.U){

            when(result(33,0)===0.U){

              HiLo:=0.U

            }.elsewhen(HiLo(31)===HiLo(63)){

              HiLo:=util.Cat(0.U(32.W),result(31,0))

            }.otherwise({

              adder.io.A:= ~result(33,0)

              adder.io.B := 0.U

              adder.io.Cin:=1.U

              HiLo:=util.Cat(0.U(32.W),adder.io.Sum(31,0))

            })

          }.otherwise({

            when(HiLo(63)===0.U){

              when(result(33,0)===0.U){

                HiLo:=util.Cat(result(65,34),0.U(32.W))

              }.elsewhen(HiLo(31)===HiLo(63)){

                HiLo:=util.Cat(result(65,34),result(31,0))

              }.otherwise({

                adder.io.A:= ~result(33,0)

                adder.io.B := 0.U

                adder.io.Cin:=1.U

                HiLo:=util.Cat(result(65,34),adder.io.Sum(31,0))

              })

            }.otherwise({

              when(result(33,0)===0.U){

                adder.io.A:= ~result(67,34)

                adder.io.B:= 1.U

                adder.io.Cin:= 0.U

                HiLo:=util.Cat(adder.io.Sum(31,0),0.U(32.W))

              }.elsewhen(HiLo(31)===HiLo(63)){

                adder.io.A:= ~result(67,34)

                adder.io.B:= 1.U

                adder.io.Cin:= 0.U

                HiLo:=util.Cat(adder.io.Sum(31,0),result(31,0))

              }.otherwise({

                adder.io.A:= ~result(33,0)

                adder.io.B := 0.U

                adder.io.Cin:=1.U

                HiLo:=util.Cat(-result(65,34),adder.io.Sum(31,0))

              })

            })

          })

          state := s_finish

        }

        is(s_finish) {

          io.complete := 1.U

          state := s_init

        }

      }

    }

    is(ctr_divu) {

      switch(state){

        is(s_init){

          regA:=util.Cat(0.U(2.W),io.A)

          regB:=util.Cat(0.U(2.W),io.B)

          result:= util.Cat(0.U(36.W),io.A)

          state:=s_acc

          cnt:=0.U

        }

        is(s_acc){

          adder.io.A:= ~regB

          adder.io.B:= result(67,34)

          adder.io.Cin:=1.U

          cnt:=cnt+1.U

          when(adder.io.Sum(33)===0.U){

            result:=util.Cat(adder.io.Sum(32,0),result(33,1),"b10".U(2.W))

          }.otherwise({

            result:=util.Cat(result(66,0),0.U(1.W))

          })

          when(cnt=/=33.U){

            state:=s_acc

          }.otherwise({

            state:=s_ready

          })

        }

        is(s_ready){

          adder.io.A:= ~regB

          adder.io.B:= result(67,34)

          adder.io.Cin:=1.U

          when(adder.io.Sum(33)===0.U){

            result:=util.Cat(adder.io.Sum(33,0),result(33,1),"b1".U(1.W))

          }.otherwise({

            result:=result(67,0)

          })

          state:=s_done

        }

        is(s_done){

          HiLo:=util.Cat(result(65,34),result(31,0))

          state:=s_finish

        }

        is(s_finish){

          io.complete:=1.U

          state:=s_init

        }

      }

    }

    is(ctr_mul) {

      switch(state) {

        is(s_init) {

          regA:=io.A

          regB:=io.B

          adder.io.A := 0.U

          adder.io.B := 0.U

          adder.io.Cin := 0.U

          result := util.Cat(0.U(34.W), B)

          HiLo:=0.U

          cnt := 0.U

          a_c := ~A

          a_2 := A << 1.U

          io.complete := 0.U

          state := s_ready

        }

        is(s_ready) {

          adder.io.A := 0.U

          adder.io.B := 0.U

          adder.io.Cin := 0.U

          a_2c := ~a_2

          state := s_acc

        }

        is(s_acc) {

          A :=  util.Cat(util.Cat(regA(31),regA(31)),regA(31,0))

          B :=  util.Cat(util.Cat(regB(31),regB(31)),regB(31,0))

          A_u := util.Cat(0.U(2.W),regA(31,0))

          B_u := util.Cat(0.U(2.W),regB(31,0))

          val dst = Wire(UInt(3.W))

          dst := util.Cat(result(1, 0), z)

          switch(dst) {

            is("b001".U) {

              adder.io.A := result(67, 34)

              adder.io.B := A

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b010".U) {

              adder.io.A := result(67, 34)

              adder.io.B := A

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b101".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_c

              adder.io.Cin := 1.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b110".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_c

              adder.io.Cin := 1.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b011".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_2

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b100".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_2c

              adder.io.Cin := 1.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b000".U) {

              adder.io.A := 0.U

              adder.io.B := 0.U

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(result(67), result(67)), result(67, 2))

            }

            is("b111".U) {

              adder.io.A := 0.U

              adder.io.B := 0.U

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(result(67), result(67)), result(67, 2))

            }

          }

          cnt := cnt + 1.U

          when(cnt =/= 16.U) {

            state := s_acc

          }.otherwise({

            state := s_done

          })

        }

        is(s_done) {

          adder.io.A := 0.U

          adder.io.B := 0.U

          adder.io.Cin := 0.U

          HiLo := result(63,0)

          state := s_finish

          cnt := 0.U

        }

        is(s_finish){

          io.complete := 1.U

          z:= 0.U

          state := s_init

        }

      }

    }

    is(ctr_mulu) {

      switch(state) {

        is(s_init) {

          regA:=io.A

          regB:=io.B

          adder.io.A := 0.U

          adder.io.B := 0.U

          adder.io.Cin := 0.U

          result := util.Cat(0.U(34.W), B_u)

          HiLo:=0.U

          cnt := 0.U

          a_c := ~A_u

          a_2 := A_u << 1.U

          io.complete := 0.U

          state := s_ready

        }

        is(s_ready) {

          adder.io.A := 0.U

          adder.io.B := 0.U

          adder.io.Cin := 0.U

          a_2c := ~a_2

          state := s_acc

        }

        is(s_acc) {

          A :=  util.Cat(util.Cat(regA(31),regA(31)),regA(31,0))

          B :=  util.Cat(util.Cat(regB(31),regB(31)),regB(31,0))

          A_u := util.Cat(0.U(2.W),regA(31,0))

          B_u := util.Cat(0.U(2.W),regB(31,0))

          val dst = Wire(UInt(3.W))

          dst := util.Cat(result(1, 0), z)

          switch(dst) {

            is("b001".U) {

              adder.io.A := result(67, 34)

              adder.io.B := A_u

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b010".U) {

              adder.io.A := result(67, 34)

              adder.io.B := A_u

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

            }

            is("b101".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_c

              adder.io.Cin := 1.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

              //              printf("101\n")

            }

            is("b110".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_c

              adder.io.Cin := 1.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

              //              printf("110\n")

            }

            is("b011".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_2

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

              //              printf("011\n")

            }

            is("b100".U) {

              adder.io.A := result(67, 34)

              adder.io.B := a_2c

              adder.io.Cin := 1.U

              z := result(1)

              result := util.Cat(util.Cat(adder.io.Sum(33), adder.io.Sum(33)), util.Cat(adder.io.Sum(33, 0), result(33, 2)))

              //              printf("100\n")

            }

            is("b000".U) {

              adder.io.A := 0.U

              adder.io.B := 0.U

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(result(67), result(67)), result(67, 2))

              //              printf("000\n")

            }

            is("b111".U) {

              adder.io.A := 0.U

              adder.io.B := 0.U

              adder.io.Cin := 0.U

              z := result(1)

              result := util.Cat(util.Cat(result(67), result(67)), result(67, 2))

              //              printf("111\n")

            }

          }

          cnt := cnt + 1.U

          when(cnt =/= 16.U) {

            state := s_acc

          }.otherwise({

            state := s_done

          })

          // printf("cnt=%d\n",cnt)

          //printf("s_acc,product(63,32)=%d,product(31,0)=%d\n",product(63,32),product(31,0))

        }

        is(s_done) {

          adder.io.A := 0.U

          adder.io.B := 0.U

          adder.io.Cin := 0.U

          HiLo := result(63,0)

          state := s_finish

          cnt := 0.U

        }

        is(s_finish){

          io.complete := 1.U

          z:= 0.U

          state := s_init

        }

      }

    }

    //逻辑运算

    is(ctr_nor) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := nor_out

    }

    is(ctr_or) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := or_out

    }

    is(ctr_and) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := and_out

    }

    is(ctr_xor) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := xor_out

    }

    is(ctr_sll) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := sll_out

    }

    is(ctr_sllv) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := sll_out

    }

    is(ctr_sra) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := sra_out

    }

    is(ctr_srav) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := sra_out

    }

    is(ctr_srl) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := srl_out

    }

    is(ctr_srlv) {

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin := 0.U

      io.ALUout := srl_out

    }

    is(ctr_lui){

      io.ALUout:=util.Cat(io.B(15,0),0.U(16.W))

    }

    //分支跳转

    is(ctr_bne){

      adder.io.A := A

      adder.io.B := ~B

      adder.io.Cin :=1.U

      when(adder.io.Sum === 0.U){

        io.cond := 0.U

      }.otherwise({

        io.cond := 1.U

      })

    }

    is(ctr_beq){

      adder.io.A := A

      adder.io.B := ~B

      adder.io.Cin :=1.U

      when(adder.io.Sum === 0.U){

        io.cond := 1.U

      }.otherwise({

        io.cond := 0.U

      })

    }

    is(ctr_bgez){

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin :=0.U

      when(io.A(31)===0.U){

        io.cond := 1.U(1.W)

      }.otherwise({

        io.cond := 0.U(1.W)

      })

    }

    is(ctr_bgtz){

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin :=0.U

      when(io.A(31)===1.U){

        io.cond:=0.U(1.W)

      }.elsewhen(io.A===0.U){

        io.cond:=0.U(1.W)

      }.otherwise({

        io.cond:=1.U(1.W)

      })

    }

    is(ctr_bltz){

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin :=0.U

      when(io.A(31)===1.U){

        io.cond:=1.U(1.W)

      }.otherwise({

        io.cond:=0.U(1.W)

      })

    }

    is(ctr_bgezal){

      adder.io.A := 4.U

      adder.io.B := B_u

      adder.io.Cin :=0.U

      io.ALUout:=adder.io.Sum(31,0)

      when(io.A(31)===0.U){

        io.cond := 1.U(1.W)

      }.otherwise({

        io.cond := 0.U(1.W)

      })

    }

    is(ctr_bltzal){

      adder.io.A := 4.U

      adder.io.B := B_u

      adder.io.Cin :=0.U

      io.ALUout:= adder.io.Sum(31,0)

      when(io.A(31)===1.U){

        io.cond:=1.U(1.W)

      }.otherwise({

        io.cond:=0.U(1.W)

      })

    }

    is(ctr_blez){

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin :=0.U

      when(io.A(31)===1.U){

        io.cond:=1.U(1.W)

      }.elsewhen(io.A===0.U){

        io.cond:=1.U(1.W)

      }.otherwise({

        io.cond:=0.U(1.W)

      })

    }

    is(ctr_jump){

      adder.io.A := 0.U

      adder.io.B := 0.U

      adder.io.Cin :=0.U

      io.cond := 1.U

    }

    is(ctr_jal){

      io.cond:=1.U

      adder.io.A:=4.U

      adder.io.B:=io.B

      adder.io.Cin:=0.U

      io.ALUout:= adder.io.Sum(31,0)

    }

    is(ctr_jr){

      io.cond:=1.U

      adder.io.A:=4.U

      adder.io.B:=io.B

      adder.io.Cin:=0.U

      io.cond:=1.U

    }

    is(ctr_jalr){

      io.cond:=1.U

      adder.io.A:=4.U

      adder.io.B:=io.B

      adder.io.Cin:=0.U

      io.ALUout:=adder.io.Sum(31,0)

    }

    is(ctr_slt){

      adder.io.A := A

      adder.io.B := ~B

      adder.io.Cin :=1.U

      when(adder.io.Sum(33)===1.U){

        io.ALUout := 1.U

      }.otherwise({

        io.ALUout := 0.U

      })

    }

    is(ctr_sltu){

      adder.io.A := A_u

      adder.io.B := ~B_u

      adder.io.Cin :=1.U

      when(adder.io.Sum(33)===1.U){

        io.ALUout := 1.U

      }.otherwise({

        io.ALUout := 0.U

      })

    }

    is(ctr_mtlo){

      HiLo:=util.Cat(HiLo(63,32),io.A)

    }

    is(ctr_mthi){

      HiLo:=util.Cat(io.A,HiLo(31,0))

    }

    is(ctr_mflo){

      io.ALUout:=HiLo(31,0)

    }

    is(ctr_mfhi){

      io.ALUout:=HiLo(63,32)

    }

    is(ctr_mtc0){
      adder.io.A:=0.U
      adder.io.B:=io.B
      adder.io.Cin:=0.U
      io.ALUout:=adder.io.Sum
    }
    is(ctr_mfc0){
      adder.io.A:= io.A
      adder.io.B:= 0.U
      adder.io.Cin:=0.U
      io.ALUout:=adder.io.Sum
    }

  }

}
