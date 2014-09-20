/*
 * PWMTest.c
 *
 * Created: 8/20/2014 6:12:39 PM
 *  Author: Zack Brandes
 */ 

/************************************************************************/
/* This code receives serial data using an ISR to control pwm outputs   */
/* of an rgb led.  Board: atmega328									    */  
/* Note: This this code is written for a common anode RGB LED, where    */ 
/* Red is wired to PD5, Blue is wired to PD6, and Green is wired to PB5 */     
/************************************************************************/

#define F_CPU 16000000
#define BAUDRATE 9600
#define BAUD_PRESCALLER (((F_CPU / (BAUDRATE * 16UL))) - 1)
#define BUFFER_SIZE 100

#include <avr/io.h>
#include <util/delay.h>
#include <stdio.h>
#include <stdlib.h>
#include <avr/interrupt.h>
#include <string.h>

//globals
volatile unsigned char byte_buffer[BUFFER_SIZE];   //Buffer that is accessed by main and ISR
volatile unsigned char cur_byte = 0;			   //Current position in byte buffer

//Functions
void USART_init(void);

/*Initialize the RX and TX pins*/
void USART_init()
{
	UBRR0H = (uint8_t)(BAUD_PRESCALLER>>8);
	UBRR0L = (uint8_t)(BAUD_PRESCALLER);
	
	#if USE_2X
	UCSR0A |= (1<<U2X0);
	#else
	UCSR0A &= ~(1<<U2X0);
	#endif
	
	UCSR0C = (1<<UCSZ01) | (1<<UCSZ00);
	UCSR0B = (1<<RXEN0) | (1<<TXEN0) | (1<<RXCIE0);
}

/*Interrupt Service Routine for receiving serial data*/
ISR(USART_RX_vect)
{
	/*Received bytes are stored in a byte_buffer for access by main execution, and the next spot in the buffer is set to '\0'*/
	while(!(UCSR0A & (1<<RXC0)));  
	byte_buffer[cur_byte] = UDR0;
	cur_byte = (cur_byte == BUFFER_SIZE - 1) ? 0 : cur_byte + 1 ;
	byte_buffer[cur_byte] = '\0';
}

int main(void)
{
	USART_init();		
	sei();				//Enable global interrupts
	
	/*Ports D5, D6, and B1 correspond to OC0B, OC0A, and OC1A.  These are PWM outputs for timers 0 and 1*/
	DDRD |= (_BV(DDD6)|_BV(DDD5)); //Set D5, D6, and B5 for outputs
	DDRB |= _BV(DDB1);
	
	//Initialize Duty Cycles: 
	OCR0A = 255;		//Blue: 0%
	OCR0B = 0;			//Red: 100%
	OCR1A = 255;		//Green: 0%
		
	//Non-inverting mode 
	TCCR0A |= (_BV(COM0A1)|_BV(COM0B1));
	TCCR1A |= _BV(COM1A1);
	
	//Fast PWM mode
	TCCR0A |= (_BV(WGM01)|_BV(WGM00));
	TCCR1A |= _BV(WGM10);
	TCCR1B |= _BV(WGM12);
	
	//Prescaling: none
	TCCR0B |= _BV(CS01);
	TCCR1B |= _BV(CS11);
	
	//Initialize globals in case of reset
	cur_byte = 0;
	byte_buffer[0] = '\0';
	
	
	unsigned char lastChar = 'x';	//Dictates what color to change
	int byte_to_process = 0;		//Current byte to process in byte buffer
    while(1)
    {		
		/* The byte buffer that is filled using the serial interrupt is processed on the basis 
		   that all non-processed bits do not have a value of '\0', and all processed bits have been set to '\0'.
		   The protocol used is simple: a change to the red intensity will be immediately preceded by the reception of 'r',
		   and likewise for blue and green.
		*/
		if(byte_buffer[byte_to_process] != '\0') //There are bytes to process
		{
			//If the processed byte is an 'r', 'g', or 'b', and the last byte received was not an 'r', 'g', or 'b'
			if(lastChar == 'x' && (byte_buffer[byte_to_process] == 'r' || byte_buffer[byte_to_process] == 'b' || byte_buffer[byte_to_process] == 'g'))
			{
				lastChar = byte_buffer[byte_to_process];
			}
			else //The last byte received was an 'r', 'g', or 'b', and thus this byte is the value
			{
				/*The received value is subtracted from 255 because the RGB LED I used was common anode, 
				  and so a duty cycle of 100% turns the LED off.
				  
				  1 was added to the value because the android program is designed never to send a value of 0,
				  as that would interfere with the buffer processing ('\0' == 0)
				  
				  Thus adding 1 allows for a duty cycle of 0% (255)
			    */
				switch (lastChar)
				{
					case 'r':
						OCR0B = 255-byte_buffer[byte_to_process]+1;
						break;
					case 'b':
						OCR0A = 255-byte_buffer[byte_to_process]+1;
						break;
					case 'g':
						OCR1A = 255-byte_buffer[byte_to_process]+1;
						break;
					default:
						break;
				}
				lastChar = 'x';				
			}			
			//Set the processed byte to '\0', so it is not processed again when the byte buffer loops back around
			byte_buffer[byte_to_process] = '\0';
			byte_to_process = (byte_to_process == BUFFER_SIZE - 1) ? 0 : byte_to_process + 1;
		}
		
    }
}