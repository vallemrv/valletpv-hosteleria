<template>
  <div class="tools-layout">
    <v-app-bar app class="app-bar" flat>
      <v-btn icon @click="$router.push('/')">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <v-toolbar-title class="app-title">Herramientas</v-toolbar-title>
    </v-app-bar>

    <div class="tools-content">
      <v-container>
        <v-row>
          <v-col cols="12" md="6" lg="4">
            <v-card class="tool-card" @click="$router.push('/tools/waiters')" hover>
              <v-card-item>
                <template v-slot:prepend>
                  <v-icon icon="mdi-account-group" color="primary" size="x-large" class="mr-4"></v-icon>
                </template>
                <v-card-title>Camareros</v-card-title>
                <v-card-subtitle>Autorizar y desautorizar acceso</v-card-subtitle>
              </v-card-item>
            </v-card>
          </v-col>
          <!-- More tools can be added here -->
        </v-row>
      </v-container>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ToolsView',
};
</script>

<style scoped>
.tools-layout {
  min-height: 100dvh;
  background-color: #f5f7fa;
}

.app-bar {
  background-color: white !important;
  border-bottom: 1px solid #e0e0e0;
}

.app-title {
  font-weight: 600;
  color: #2d3748;
}

.tools-content {
  padding-top: 80px; /* Space for app bar */
}

.tool-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.tool-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}
</style>
