import { ApplicationConfig, provideZoneChangeDetection, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { CoreModule } from './core/core-module'; // Adjusted import name

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    importProvidersFrom(CoreModule) // Import providers from CoreModule
    // HttpClientModule would typically be provided here too,
    // e.g., via importProvidersFrom(HttpClientModule) or provideHttpClient()
    // For now, only CoreModule as per subtask's focus on module structure.
  ]
};
