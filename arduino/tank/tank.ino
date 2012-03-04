int E1 = 6; //M1 Speed Control
int E2 = 5; //M2 Speed Control
int M1 = 8; //M1 Direction Control
int M2 = 7; //M2 Direction Control
byte incomingByte;
int stopped = 1;


byte stack[512];
int i=0;

void setup()
{
  // Motors
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(8, OUTPUT);
  
  // LEDS
  pinMode(13, OUTPUT);
  
  Serial.begin(9600);
  //analogWrite (E1,100);
  //analogWrite (E2,100);
}

void stopMotors(){
  analogWrite (E1,0);
  analogWrite (E2,0);
  stopped = 1;
}

void startMotors(){
  analogWrite(E1,100); 
  analogWrite(E2,100);
  stopped = 0;
}

void lightPin(bool on) {
  if (on) {
    digitalWrite(13, HIGH);
  } else {
    digitalWrite(13, LOW);
  }
}

int executeCommand(int offset){
  //<Motor 1(Right) or 2(Left)><Direction Forward=F, Reverse=R><\n>
  if(stack[offset] == 'S'){
    stopMotors();
    stack[offset] = 0;
    return 1;
  } else if(stack[offset] == '1') {
    analogWrite(E1, 100);
    if(stack[offset+1] == 'R') {
      //reverse right
      //if(stopped) startMotors();
      digitalWrite(M1,HIGH);
      Serial.print('1R');
    }else{
      //forward left
      //if(stopped) startMotors();
      digitalWrite(M1,LOW);
      Serial.print('1F');
    }
    stack[offset] = 0;
    stack[offset+1] = 0;
    return 2;
  }else if(stack[offset] == '2'){
      analogWrite(E2, 100);
      if(stack[offset+1] == 'R'){
        //reverse left
        //if(stopped) startMotors();
        digitalWrite(M2,HIGH);
        Serial.print('2R');
      }else{
        //forward left
        //if(stopped) startMotors();
        digitalWrite(M2,LOW);
        Serial.print('2F');
      }
      stack[offset] = 0;
      stack[offset+1] = 0;
      return 2;
  } else {
    return -1;
  }
  //return 1;
}

void loop()
{
  int evalOffset;
  int consumed;
  
  //int content;

  //analogWrite (E1,100);
  //analogWrite (E2,100);

  //analogWrite(M1, 100);
  //analogWrite(M2, 100);  


  /*
  analogWrite (E1,100);
  analogWrite (E2,100);

  analogWrite(M1, 100);
  analogWrite(M2, 100);  
  */
  //startMotors();
  //digitalWrite(M2,LOW);
  //delay(100)

/*
  int content;
  while(true) {
    delay(100);
    if (Serial.available() > 0) {
      content = Serial.read();
      Serial.write(content);
    }
  }
  */

  if (Serial.available() > 0) {
    incomingByte = Serial.read();
    if(incomingByte == '\n'){
      evalOffset = 0;
      while (true) {
        consumed = executeCommand(evalOffset);
        if (consumed < 0)
          break;
        
        evalOffset = evalOffset + consumed;
      }
      Serial.print('\n');
      lightPin(false);
      i = 0;
    }else{
      lightPin(true);
      stack[i] = incomingByte;
      i++;
    }
  }
}
