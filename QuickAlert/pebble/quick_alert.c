#include <pebble.h>
  
#define PASSCODE_PERSIST_KEY 17
#define PAUSE_LEN 1000
#define BUFFER_LEN 20

static Window *window;
static TextLayer *text_layer;

static bool s_button_held;
static bool s_on_passcode;
static bool s_on_main_screen;

static void s_display_unlock_msg(void *data);
static void s_reset_app(void *data);

static int passcode_counter;

void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  char *buffer = malloc(BUFFER_LEN);
  if (s_on_passcode) {
    snprintf(buffer, BUFFER_LEN, "Passcode char %d", passcode_counter + 1);
    passcode_counter++;
    if (passcode_counter == 5) {
      s_display_unlock_msg(NULL);
    }
    text_layer_set_text(text_layer, buffer);
  }
}

void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  char *buffer = malloc(BUFFER_LEN);
  if (s_on_passcode) {
    snprintf(buffer, BUFFER_LEN, "Passcode char %d", passcode_counter + 1);
    passcode_counter++;
    if (passcode_counter == 5) {
      s_display_unlock_msg(NULL);
    }
    text_layer_set_text(text_layer, buffer);
  }
}

void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  char *buffer = malloc(BUFFER_LEN);
  if (s_on_passcode) {
    snprintf(buffer, BUFFER_LEN, "Passcode char %d", passcode_counter + 1);
    passcode_counter++;
    if (passcode_counter == 5) {
      s_display_unlock_msg(NULL);
    }
    text_layer_set_text(text_layer, buffer);
  }
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

static void s_display_unlock_msg(void *data) {
  text_layer_set_text(text_layer, "Passcode entered");
  app_timer_register(PAUSE_LEN, s_reset_app, NULL);
}

static void s_reset_app(void *data) {
  s_on_main_screen = true;
  s_on_passcode = false;
  s_button_held = false;
  passcode_counter = 0;
  text_layer_set_text(text_layer, "Hold button until safe");
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
  s_reset_app(NULL);
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
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