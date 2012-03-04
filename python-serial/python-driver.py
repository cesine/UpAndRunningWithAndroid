
import serial
import time

ser = serial.Serial('COM3', 9600)

def TurnRight():
  ser.write('1F')

def TurnLeft():
  ser.write('1F')
  
def Forward():
  print("Forward");
  ser.write('1F2F')
  
def Reverse():
  ser.write('1R2F')
  
def Stop():
  ser.write('S')
 
def Flush():
  print("Flush")
  ser.write('\n')
  
def RotateRight():
  print("RotateRight")
  ser.write('1R2F')
  
def RotateLeft():
  print("RotateLeft")
  ser.write('1F2R')

def Figure8():
  direction = 0
  print("Figure8\n")
  for x in range(4) :
    Forward()
    Flush()
    time.sleep(1)
    RotateRight()
    Flush()
    time.sleep(0.5)  
  
  # while direction < 100:
  #  Forward()
  #  time.sleep(1)
  #  RotateRight()
  #  FlushDirections()   
   
  #while direction < 100:
  #  Forward()
  #  time.sleep(1)
  #  RotateLeft()
  #  FlushDirections()
  Stop()
  Flush()
    
if __name__ == '__main__':
  ser.write('S')
  Figure8()
  Stop();
  ser.close()

