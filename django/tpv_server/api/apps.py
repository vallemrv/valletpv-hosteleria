from django.apps import AppConfig
import os
from django.conf import settings

class ApiConfig(AppConfig):
    name = 'api'

    def ready(self):
        try:
            # Construct paths
            server_name = getattr(settings, 'EMPRESA', 'default')
            
            # Correctly use MEDIA_ROOT as requested
            base_path = os.path.join(settings.MEDIA_ROOT, 'planing')
            clicables_path = os.path.join(base_path, 'elementos_clicables')
            no_clicables_path = os.path.join(base_path, 'no_clicable')

            # Create directories
            dirs_to_create = [base_path, clicables_path, no_clicables_path]
            
            for path in dirs_to_create:
                if not os.path.exists(path):
                    try:
                        os.makedirs(path)
                        print(f"Created directory: {path}")
                    except OSError as e:
                        print(f"Error creating directory {path}: {e}")
                        
        except Exception as e:
            print(f"Error initializing planing directories: {e}")
