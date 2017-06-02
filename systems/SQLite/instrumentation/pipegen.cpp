#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Instructions.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Type.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/IRBuilder.h"
#include <vector>
#include <fstream>
#include <unordered_set>

using namespace llvm;

namespace{
    struct pipegen_instrumentation1 : public ModulePass{
        static char ID;  
        Function *hook;
        int idx = 0;

        pipegen_instrumentation1() : ModulePass(ID) {}

        virtual bool runOnModule(Module &M)
        {
            Constant *hookFunc;
            hookFunc = M.getOrInsertFunction(
                "pipegen_instrumentation_logging", 
                FunctionType::FunctionType::getVoidTy(M.getContext()),
                Type::getInt32Ty(M.getContext()),
                Type::getInt64Ty(M.getContext())
                );
            hook = cast<Function>(hookFunc);

            for(Module::iterator F = M.begin(), E = M.end(); F!= E; ++F)
            {
                for(Function::iterator BB = F->begin(), E = F->end(); BB != E; ++BB)
                {
                    pipegen_instrumentation1::runOnBasicBlock(BB);
                }
            }

            return false;
        }
        virtual bool runOnBasicBlock(Function::iterator &BB)
        {
            for(BasicBlock::iterator BI = BB->begin(), BE = BB->end(); BI != BE; ++BI)          
            {
                if(isa<CallInst>(&(*BI)) )
                {
                    CallInst *CI = dyn_cast<CallInst>(BI); 
                    // mcsema will change fopen to _fopen in their decompiled version   
                    if (CI->getCalledFunction()->getName() == "_fopen") {
                        IRBuilder<> builder(&(*BI));
                        ConstantInt *idxParam = builder.getInt32(idx++);
                        Value *filenameParam = CI->getArgOperand(0);
                        std::vector<Value *> args(2);
                        args[0] = idxParam;
                        args[1] = filenameParam;
                        auto *newInst = CallInst::Create(hook, args);
                        newInst->insertBefore(CI);
                    }              
                }
                        
            }
            return true;
        }
    };
}

namespace{
    struct pipegen_instrumentation2 : public ModulePass{
        static char ID;  
        Function *hook;
        int idx = 0;
        std::unordered_set<int> relatedIndice;

        pipegen_instrumentation2() : ModulePass(ID) {}

        virtual bool runOnModule(Module &M)
        {
            std::ifstream infile("pipegen_instrumentation.log");
            std::string path;
            int currentIndex;
            std::string unrelated = ".sqliterc";

            // keep the related call index
            // related call: file name is not .sqliterc
            while (infile >> currentIndex >> path) {
                if (path.size() < unrelated.size() || 
                    path.compare(path.size() - unrelated.size(), unrelated.size(), unrelated) != 0) {
                    relatedIndice.insert(currentIndex);
                }
            }

            Constant *hookFunc;
            hookFunc = M.getOrInsertFunction(
                "_pipegen_fopen", 
                FunctionType::FunctionType::getInt64Ty(M.getContext()),
                Type::getInt64Ty(M.getContext()),
                Type::getInt64Ty(M.getContext())
                );
            hook = cast<Function>(hookFunc);
            M.getOrInsertFunction(
                "pipegen_fopen", 
                FunctionType::FunctionType::getInt64Ty(M.getContext()),
                Type::getInt64Ty(M.getContext()),
                Type::getInt64Ty(M.getContext())
                );

            for(Module::iterator F = M.begin(), E = M.end(); F!= E; ++F)
            {
                for(Function::iterator BB = F->begin(), E = F->end(); BB != E; ++BB)
                {
                    pipegen_instrumentation2::runOnBasicBlock(BB);
                }
            }

            // this is the section to cater for mcsema way of function call
            M.appendModuleInlineAsm(StringRef(".globl pipegen_fopen;"));
            M.appendModuleInlineAsm(StringRef(".globl _pipegen_fopen;"));
            M.appendModuleInlineAsm(StringRef(".type _pipegen_fopen,@function"));
            M.appendModuleInlineAsm(StringRef("_pipegen_fopen:"));
            M.appendModuleInlineAsm(StringRef(".cfi_startproc;"));
            M.appendModuleInlineAsm(StringRef("pushq %rax;"));
            M.appendModuleInlineAsm(StringRef("leaq pipegen_fopen(%rip), %rax;"));
            M.appendModuleInlineAsm(StringRef("xchgq (%rsp), %rax;"));
            M.appendModuleInlineAsm(StringRef("jmp __mcsema_detach_call;"));
            M.appendModuleInlineAsm(StringRef("0:"));
            M.appendModuleInlineAsm(StringRef(".size _pipegen_fopen,0b-_pipegen_fopen;"));
            M.appendModuleInlineAsm(StringRef(".cfi_endproc;"));

            return false;
        }
        virtual bool runOnBasicBlock(Function::iterator &BB)
        {
            for(BasicBlock::iterator BI = BB->begin(), BE = BB->end(); BI != BE; ++BI)          
            {
                if(isa<CallInst>(&(*BI)) )
                {
                    CallInst *CI = dyn_cast<CallInst>(BI);    
                    if (CI->getCalledFunction()->getName() == "_fopen") {
                        if (relatedIndice.find(idx) != relatedIndice.end()) {
                            CI->setCalledFunction(hook->getFunctionType(), hook);
                        }
                        idx++;
                    }              
                }
                        
            }
            return true;
        }
    };
}


char pipegen_instrumentation1::ID = 1;
static RegisterPass<pipegen_instrumentation1> X1("instrumentation1", "Pipegen instrumentation Pass",
                                                         false /* Only looks at CFG */,
                                                         false /* Analysis Pass */);

char pipegen_instrumentation2::ID = 2;
static RegisterPass<pipegen_instrumentation2> X2("instrumentation2", "Pipegen instrumentation Pass",
                                                         false /* Only looks at CFG */,
                                                         false /* Analysis Pass */);