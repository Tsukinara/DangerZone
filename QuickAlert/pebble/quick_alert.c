#include <pebble.h>
  
#define PASSCODE_PERSIST_KEY 17
#define TIMERLEN_PERSIST_KEY 18
  
#define PAUSE_LEN 1000
#define BUFFER_LEN 20
#define PASSCODE_LEN 5

static Window *window;
static TextLayer *text_layer;
static TextLayer *code_layer;

static bool s_button_held;
static bool s_on_passcode;
static bool s_on_main_screen;

static void s_display_unlock_msg(void *data);
static void s_display_error_msg(void *data);
static void s_update_passcode(void *data);
static void s_reset_app(void *data);
static bool s_check_code(void *data);

static int s_passcode[PASSCODE_LEN] = {1, 2, 3, 2, 1};
static int s_entry[PASSCODE_LEN];

static int passcode_counter;

void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  char *buffer = malloc(BUFFER_LEN);
  if (s_on_passcode) {
    snprintf(buffer, BUFFER_LEN, "Passcode char %d", passcode_counter + 1);
    s_entry[passcode_counter] = 2;
    passcode_counter++;
    s_update_passcode(NULL);
    if (passcode_counter == PASSCODE_LEN) {
      if(s_check_code(s_entry)) {
        s_display_unlock_msg(NULL);
      } else {
        s_display_error_msg(NULL);
      }
    } else {
      text_layer_set_text(text_layer, buffer);
    }
  }
  free(buffer);
}

void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  char *buffer = malloc(BUFFER_LEN);
  if (s_on_passcode) {
    snprintf(buffer, BUFFER_LEN, "Passcode char %d", passcode_counter + 1);
    s_entry[passcode_counter] = 1;
    passcode_counter++;
    s_update_passcode(NULL);
    if (passcode_counter == PASSCODE_LEN) {
      if(s_check_code(s_entry)) {
        s_display_unlock_msg(NULL);
      } else {
        s_display_error_msg(NULL);
      }
    } else {
      text_layer_set_text(text_layer, buffer);
    }
  }
  free(buffer);
}

void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  char *buffer = malloc(BUFFER_LEN);
  if (s_on_passcode) {
    snprintf(buffer, BUFFER_LEN, "Passcode char %d", passcode_counter + 1);
    s_entry[passcode_counter] = 3;
    passcode_counter++;
    s_update_passcode(NULL);
    if (passcode_counter == PASSCODE_LEN) {
      if(s_check_code(s_entry)) {
        s_display_unlock_msg(NULL);
      } else {
        s_display_error_msg(NULL);
      }
    } else {
      text_layer_set_text(text_layer, buffer);
    }
  }
  free(buffer);
}

void select_long_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (s_on_main_screen) {
    text_layer_set_text(text_layer, "Release when safe");
    s_button_held = true;
    s_on_main_screen = false;
  }
}

void select_long_click_release_handler(ClickRecognizerRef recognizer, void *context) {
  if (s_button_held) {
    text_layer_set_text(text_layer, "Enter passcode");
    s_button_held = false;
    s_on_passcode = true;
  }
}

static bool s_check_code(void *data) {
  int i;
  for (i = 0; i < PASSCODE_LEN; i++) {
    if (s_passcode[i] != s_entry[i]) {
      return false;
    }
  }
  return true;
}

static void s_display_unlock_msg(void *data) {
  text_layer_set_text(text_layer, "Passcode correct");
  layer_mark_dirty(text_layer_get_layer(text_layer));
  s_reset_app(NULL);
}

static void s_update_passcode(void *data) {
  char *buffer = malloc(PASSCODE_LEN + 1);
  int i;
  for (i = 0; i < passcode_counter; i++) {
    buffer[i] = s_entry[i] + 48;
  }
  text_layer_set_text(code_layer, buffer);
}
  
static void s_display_error_msg(void *data) {  
  int i;
  text_layer_set_text(text_layer, "ERROR: INCORRECT PASSCODE");
  passcode_counter = 0;
  for (i = 0; i < PASSCODE_LEN; i++) {
    s_entry[i] = 0;
  }
  text_layer_set_text(text_layer, "Enter passcode");
  text_layer_set_text(code_layer, "");
}

static void s_reset_app(void *data) {
  s_on_main_screen = true;
  s_on_passcode = false;
  s_button_held = false;
  passcode_counter = 0;
  text_layer_set_text(text_layer, "Hold button until safe");
  text_layer_set_text(code_layer, "");
}

static void click_config_provider(void *context) {
  // single click handling
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
  
  
  // long click handling
  window_long_click_subscribe(BUTTON_ID_SELECT, 700, select_long_click_handler, select_long_click_release_handler);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);
  
  text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 20 } });
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
  
  code_layer = text_layer_create((GRect) { .origin = { 20, 10 }, .size = {bounds.size.w-20, 25 } });
  text_layer_set_text_alignment(code_layer, GTextAlignmentLeft);
  layer_add_child(window_layer, text_layer_get_layer(code_layer));
  
  s_reset_app(NULL);
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
  text_layer_destroy(code_layer);
}

static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}