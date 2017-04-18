#ifndef _LINKEDLIST_H_
#define _LINKEDLIST_H_

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>

struct node {
   void *data;
   int key;
   struct node *next;
};

typedef struct ll {
   struct node *head;
} *LinkedList;

LinkedList init_linkedlist() {
   LinkedList l = (LinkedList)malloc(sizeof(struct ll));
   l->head = NULL;
   return l;
}

//insert link at the first location
void insertFirst(LinkedList l, int key, void *data) {
   //create a link
   struct node *link = (struct node*) malloc(sizeof(struct node));
	
   link->key = key;
   link->data = data;
	
   //point it to old first node
   link->next = l->head;
	
   //point first to new first node
   l->head = link;
}

//delete first item
struct node* deleteFirst(LinkedList l) {

   //save reference to first link
   struct node *tempLink = l->head;
	
   //mark next to first link as first 
   l->head = l->head->next;
	
   //return the deleted link
   return tempLink;
}

//is list empty
bool isEmpty(LinkedList l) {
   return l->head == NULL;
}

#endif /* #ifndef _LINKEDLIST_H_ */