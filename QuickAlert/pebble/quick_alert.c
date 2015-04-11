#include <pebble.h>

#define PAUSE_LEN 1000
#define BUFFER_LEN 30
#define PASSCODE_LEN 5
#define UPDATE_INTV 50
#define PASSCODE_PERSIST_KEY 17
#define TIMERLEN_PERSIST_KEY 18

static Layer *s_main_layer;

static int s_curr_state;
static bool s_passcode_defined;

static int s_timer_len;
static int passcode_counter;
static int s_timer_curr;

static void s_display_unlock_msg(void *data);
static void s_display_error_msg(void *data);
static void s_call_police(void *data);
static void s_reset_app(void* data);
static bool s_check_code(void *data);

static char s_passcode[PASSCODE_LEN + 1] = "12321";
static char s_entry[PASSCODE_LEN + 1];

static AppTimer *call_timer;


void main_init(void) {
  s_reset_app(NULL);
  
  if (persist_exists(TIMERLEN_PERSIST_KEY)) {
    s_timer_len = persist_read_int(TIMERLEN_PERSIST_KEY);
  } else {
    s_timer_len = 10*1000;
  }
  
  if (persist_exists(PASSCODE_PERSIST_KEY)) {
    s_passcode_defined = true;
    persist_read_string(PASSCODE_PERSIST_KEY, s_passcode, PASSCODE_LEN + 1);
  } else {
    s_passcode_defined = true;
  }
  
}

void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (s_curr_state == 2) {
    s_entry[passcode_counter] = '2';
    passcode_counter++;
  }
}

void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (s_curr_state == 2) {
    s_entry[passcode_counter] = '1';
    passcode_counter++;
  }
}

void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (s_curr_state == 2) {
    s_entry[passcode_counter] = '3';
    passcode_counter++;
  }
}

void select_long_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (s_curr_state == 0) {
    s_curr_state ++;
  }
}

void select_long_click_release_handler(ClickRecognizerRef recognizer, void *context) {
  if (s_curr_state == 1) {
    call_timer = app_timer_register(s_timer_len, s_call_police, NULL);
    s_timer_curr = s_timer_len;
    s_curr_state ++;
  }
}

static bool s_check_code(void *data) {
  if(strcmp(s_passcode, s_entry) != 0) {
    return false;
  }
  return true;
}

static void s_display_unlock_msg(void *data) {
  s_curr_state = 3;
  app_timer_register(PAUSE_LEN, s_reset_app, NULL);
  app_timer_cancel(call_timer);
}

static void s_display_error_msg(void *data) {  
  int i;
  s_curr_state = 4;
  passcode_counter = 0;
  for (i = 0; i < PASSCODE_LEN; i++) {
    s_entry[i] = 0;
  }
}

void main_render(Layer *layer, GContext *ctx) {
  char *remaining;
  graphics_context_set_text_color(ctx, GColorBlack);
  if(!s_passcode_defined) {
    graphics_draw_text(ctx, "Define a passcode using the companion app first", fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,60}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
  } else switch (s_curr_state) {
      case 0:
        graphics_draw_text(ctx, "Hold button until safe", fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,60}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
        break;
      case 1:
        graphics_draw_text(ctx, "Release when safe", fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,60}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
        break;
      case 2:
        graphics_draw_text(ctx, "Enter passcode", fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,60}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
        remaining = malloc(BUFFER_LEN + 1);
        snprintf(remaining, BUFFER_LEN, "Timer: %ds", (int)(((double)s_timer_curr)/1000.0));
        graphics_draw_text(ctx, remaining, fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,100}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
        break;
      case 3:
        graphics_draw_text(ctx, "Alert canceled", fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,60}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
        break;
      case 4:
        graphics_draw_text(ctx, "Incorrect passcode. \nRe-enter passcode", fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,60}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
        remaining = malloc(BUFFER_LEN + 1);
        snprintf(remaining, BUFFER_LEN, "Timer: %ds", (int)(((double)s_timer_curr)/1000.0));
        graphics_draw_text(ctx, remaining, fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,100}, .size = {144,25}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);    
        break;
      case 5:
        graphics_draw_text(ctx, "CALLING", fonts_get_system_font(FONT_KEY_BITHAM_30_BLACK), (GRect) {.origin = {0,0}, .size = {144,60}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
        break;
      default:
        graphics_draw_text(ctx, "undefined", fonts_get_system_font(FONT_KEY_GOTHIC_18), (GRect) {.origin = {0,0}, .size = {144,60}}, GTextOverflowModeWordWrap, GTextAlignmentCenter, NULL);
  }
}

void main_update(void *data) {
  if (s_curr_state == 2 || s_curr_state == 4) {
    s_timer_curr -= UPDATE_INTV;
    if (passcode_counter == PASSCODE_LEN) {
      if (s_check_code(s_entry)) {
        s_display_unlock_msg(NULL);
      } else {
        s_display_error_msg(NULL);
      }
    }
  }
  layer_mark_dirty(s_main_layer);
  app_timer_register(UPDATE_INTV, main_update, data);
}

void main_start(Layer *layer) {
  s_main_layer = layer;
  layer_set_update_proc(s_main_layer, main_render);
  
  app_timer_register(UPDATE_INTV, main_update, NULL);
}

static void s_call_police(void *data) {
  s_curr_state = 5;
  // code to signal phone here
}

static void s_reset_app(void *data) {
  s_curr_state = 0;
  passcode_counter = 0;
}
