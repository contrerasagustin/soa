//tiempos
#define TIME_TO_REAL_SEM 15000
#define TIME_TO_SENSOR_HIGH 10
#define TIME_TO_SENSOR_LOW 2
//#define TIME_TO_REACTIVE 9000
#define TIME_TO_REACTIVE 12000
// #define TIME_CHANGE_FIRST_YELLOW 3000
//#define TIME_CHANGE_SECOND_YELLOW 6000 
#define TIME_CHANGE_FIRST_YELLOW 6000
#define TIME_CHANGE_SECOND_YELLOW 9000
#define TIME_TO_START_TO_CHANGE 3000

//distancias
#define DISTANCE_TO_ACTIVE_SENSOR_MAX 200
#define DISTANCE_TO_ACTIVE_SENSOR_MIN 10

//values
#define MAX_VALUE 1023

//estados
#define INIT 0
#define SEM_A_GREEN 1
#define SEM_B_GREEN 2
#define UNAVAILABLE_SMART_SEM_A 3
#define UNAVAILABLE_SMART_SEM_B 4

//eventos
#define TIMEOUT 0
#define CHANGE_TO_SEM_A 1
#define CHANGE_TO_SEM_B 2
#define STANDBY 3
#define CHANGE_FIRST_YELLOW 4
#define CHANGE_SECOND_YELLOW 5
#define CHANGE_TO_START_TO_CHANGE 8
#define SMART_SEM_OFF 6
#define SMART_SEM_ON 7

//funcionamiento
#define ACTIVE true
#define INACTIVE false
#define TIME_TO_DISTANCE 58
#define LED_ON 255
#define LED_OFF 0
#define MIN_POTENTIOMETER 0
#define MAX_POTENTIOMETER 1023
#define NO_MSG 0;
int state;
int event;

//luces del semaforo
int led_red_a = 8;
int led_green_a = 9;
int led_red_b = 10;
int led_green_b = 11;

//botones de cruce
int boton_sem_a = 2;
int boton_sem_b = 3;
bool is_button_a_pressed;
bool is_button_b_pressed;

//modificador de la distancia
int potentiometer = 2;

//variables de tiempo
long time_now;
long time_change;
long time_wait;
long time_sensor;
long time_reactive;
bool standard_sem;
bool reactive_sem;

//sensores
int trigger_sem_a = 7;
int echo_sem_a = 6;
int trigger_sem_b = 5;
int echo_sem_b = 4;
bool car_in_sensor_a;
bool car_in_sensor_b;
int distance_to_active_sensor;

//bluetooth
#include <SoftwareSerial.h>
int txd=13;
int rxd=12;
SoftwareSerial BT(rxd, txd);


void setup()
{
  state = INIT;
  is_button_a_pressed = INACTIVE;
  is_button_b_pressed = INACTIVE;
  standard_sem = INACTIVE;
  car_in_sensor_a = INACTIVE;
  car_in_sensor_b = INACTIVE;
  pinMode(led_red_a, OUTPUT);
  pinMode(led_green_a, OUTPUT);
  pinMode(led_red_b, OUTPUT);
  pinMode(led_green_b, OUTPUT);
  attachInterrupt(digitalPinToInterrupt(boton_sem_a), push_button_a, RISING);
  attachInterrupt(digitalPinToInterrupt(boton_sem_b), push_button_b, RISING);
  pinMode(echo_sem_a,INPUT);
  pinMode(trigger_sem_a,OUTPUT);
  pinMode(echo_sem_b,INPUT);
  pinMode(trigger_sem_b,OUTPUT);
  state=INIT;
  event=STANDBY;
  BT.begin(9600);
  Serial.begin(9600);
}

void loop() 
{
  state_machine();
  event = get_event();
}

int get_event()
{
  time_now = millis();
  int rx=rx_bluetooth();
  if(rx == SMART_SEM_OFF)
  {
    return SMART_SEM_OFF;
  }
  if(rx == SMART_SEM_ON)
  {
    return SMART_SEM_ON;
  }
  if ((time_now - time_wait) < TIME_TO_REACTIVE)
  {
    if ((time_now - time_wait) < TIME_CHANGE_FIRST_YELLOW)
    {
      return CHANGE_FIRST_YELLOW;
    }
    if ((time_now - time_wait) < TIME_CHANGE_SECOND_YELLOW)
    {
      return CHANGE_SECOND_YELLOW;
    }
    return STANDBY;
  }

  if (is_button_a_pressed)
  {
    is_button_a_pressed=INACTIVE;
    standard_sem=INACTIVE;
    return CHANGE_TO_SEM_A;
  }

  if (is_button_b_pressed)
  {
    is_button_b_pressed=INACTIVE;
    standard_sem=INACTIVE;
    return CHANGE_TO_SEM_B;
  }
  car_in_sensor_a = register_sensor(trigger_sem_a,echo_sem_a,get_potentiometer());
  car_in_sensor_b = register_sensor(trigger_sem_b,echo_sem_b,get_potentiometer());
  
  if(car_in_sensor_a && !car_in_sensor_b)
  {
    standard_sem=INACTIVE;
    return CHANGE_TO_SEM_A;
  }
  if(!car_in_sensor_a && car_in_sensor_b)
  {
    standard_sem=INACTIVE;
    return CHANGE_TO_SEM_B;
  }
  
  if (!standard_sem)
  {
    time_change = millis();
    standard_sem = ACTIVE;
    return STANDBY;
  }
  if ((time_now - time_change) >= TIME_TO_REAL_SEM)
  {
    standard_sem = INACTIVE;
    return TIMEOUT;
  }
  return STANDBY;
}

void state_machine()
{
  switch(state)
  {
    case INIT:
      state = SEM_A_GREEN;
    break;
    case SEM_A_GREEN:
      switch (event)
      {
        case STANDBY:
          light_green_sem_a();
        break;
        case TIMEOUT:
          state = SEM_B_GREEN;
          lock_changes();
          light_green_sem_b();
        break;
        case CHANGE_TO_SEM_A:
          state = SEM_A_GREEN;
          light_green_sem_a();
        break;
        case CHANGE_TO_SEM_B:
          state = SEM_B_GREEN;
          lock_changes();
          light_green_sem_b();
        break;
        case CHANGE_FIRST_YELLOW:
          light_yellow_sem_b();
        break;
        case CHANGE_SECOND_YELLOW:
          light_yellow_sem_a();
        break;
        case SMART_SEM_OFF:
          state=UNAVAILABLE_SMART_SEM_A;
      }
    break;
    case SEM_B_GREEN:
      switch (event)
      {
        case STANDBY:
          light_green_sem_b();
        break;
        case TIMEOUT:
          state = SEM_A_GREEN;
          lock_changes();
          light_green_sem_a();
        break;
        case CHANGE_TO_SEM_A:
          state = SEM_A_GREEN;
          lock_changes();
          light_green_sem_a();
        break;
        case CHANGE_TO_SEM_B:
          state = SEM_B_GREEN;
          light_green_sem_b();
        break;
        case CHANGE_FIRST_YELLOW:
          light_yellow_sem_a();
        break;
        case CHANGE_SECOND_YELLOW:
          light_yellow_sem_b();
        break;
        case SMART_SEM_OFF:
          state=UNAVAILABLE_SMART_SEM_B;
        break;
      }
    break;
    case UNAVAILABLE_SMART_SEM_A:
      switch (event)
      {
        case STANDBY:
          light_green_sem_a();
        break;
        case TIMEOUT:
          state = UNAVAILABLE_SMART_SEM_B;
          lock_changes();
          light_green_sem_b();
        break;
        case CHANGE_FIRST_YELLOW:
          light_yellow_sem_b();
        break;
        case CHANGE_SECOND_YELLOW:
          light_yellow_sem_a();
        break;
        case SMART_SEM_ON:
          state=SEM_A_GREEN;
        break;
      }
    break;
    case UNAVAILABLE_SMART_SEM_B:
      switch (event)
      {
        case STANDBY:
          light_green_sem_b();
        break;
        case TIMEOUT:
          state = UNAVAILABLE_SMART_SEM_A;
          lock_changes();
          light_green_sem_a();
        break;
        case CHANGE_FIRST_YELLOW:
          light_yellow_sem_a();
        break;
        case CHANGE_SECOND_YELLOW:
          light_yellow_sem_b();
        break;
        case SMART_SEM_ON:
          state=SEM_B_GREEN;
        break;
      }
    break;
  }
}

void push_button_a ()
{
  is_button_a_pressed = ACTIVE;
}

void push_button_b ()
{
  is_button_b_pressed = ACTIVE;
}

void light_green_sem_a()
{
  analogWrite(led_red_a,LED_OFF);
  analogWrite(led_green_a,LED_ON);
  analogWrite(led_red_b,LED_ON);
  analogWrite(led_green_b,LED_OFF);
}

void light_yellow_sem_a()
{
  analogWrite(led_red_a,LED_ON);
  analogWrite(led_green_a,LED_ON);
  analogWrite(led_red_b,LED_ON);
  analogWrite(led_green_b,LED_OFF);
}

void light_green_sem_b()
{
  analogWrite(led_red_a,LED_ON);
  analogWrite(led_green_a,LED_OFF);
  analogWrite(led_red_b,LED_OFF);
  analogWrite(led_green_b,LED_ON);
}

void light_yellow_sem_b()
{
  analogWrite(led_red_a,LED_ON);
  analogWrite(led_green_a,LED_OFF);
  analogWrite(led_red_b,LED_ON);
  analogWrite(led_green_b,LED_ON);
}

bool register_sensor(int trigger, int echo, int distance_to_active)
{
  digitalWrite(trigger,LOW);  
  delayMicroseconds(TIME_TO_SENSOR_LOW);
  digitalWrite(trigger,HIGH);
  delayMicroseconds(TIME_TO_SENSOR_HIGH);
  digitalWrite(trigger,LOW);
  long time=pulseIn(echo,HIGH);
  long current_distance=time/TIME_TO_DISTANCE;
  if(current_distance <= distance_to_active)
  {
    return ACTIVE;
  }
  return INACTIVE;
}

void lock_changes()
{
    time_wait = millis();
}

int get_potentiometer()
{
  int value = analogRead(potentiometer);
  distance_to_active_sensor = map(value,MIN_POTENTIOMETER,MAX_POTENTIOMETER,DISTANCE_TO_ACTIVE_SENSOR_MIN,DISTANCE_TO_ACTIVE_SENSOR_MAX);
  return distance_to_active_sensor;
}

int rx_bluetooth()
{

  if(state == SEM_A_GREEN){
    BT.write("V~");
  }
  else
  {
    if(state == SEM_B_GREEN){
        .write("R~");
    }
  } 

  if (BT.available()>0)
  {
    char dato=BT.read();
    if(dato == 'S')
    {
      Serial.println("Recibi S");
      return SMART_SEM_ON;

    }
    if(dato == 'N')
    {
      Serial.println("Recibi N");
      return SMART_SEM_OFF;

      //BT.write("Me llego la N");
    }
  }

  return NO_MSG;
}