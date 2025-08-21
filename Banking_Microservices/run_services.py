#!/usr/bin/env python3
"""
Run Services Script - Helps manage the microservices for the banking application
"""

import os
import sys
import subprocess
import argparse
import signal
import time
import importlib.util

def load_module(module_name, file_path):
    """Load a module from file path"""
    spec = importlib.util.spec_from_file_location(module_name, file_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

def parse_args():
    parser = argparse.ArgumentParser(description='Run Banking Microservices')
    parser.add_argument('--no-auth', action='store_true', help='Do not run the authentication service')
    parser.add_argument('--no-account', action='store_true', help='Do not run the account service')
    parser.add_argument('--no-transaction', action='store_true', help='Do not run the transaction service')
    parser.add_argument('--no-reporting', action='store_true', help='Do not run the reporting service')
    parser.add_argument('--no-gateway', action='store_true', help='Do not run the API gateway')
    return parser.parse_args()

def run_services(args):
    """Run services based on arguments"""
    processes = []
    
    # Create necessary directories for databases
    base_dir = os.path.dirname(os.path.abspath(__file__))
    for service_dir in ['auth_service', 'account_service', 'transaction_service', 'reporting_service']:
        service_path = os.path.join(base_dir, service_dir)
        if not os.path.exists(service_path):
            os.makedirs(service_path)
    
    # Set environment variables for database URLs with absolute paths
    os.environ["AUTH_DATABASE_URL"] = f"sqlite:///{os.path.join(base_dir, 'auth_service', 'auth.db')}"
    os.environ["ACCOUNT_DATABASE_URL"] = f"sqlite:///{os.path.join(base_dir, 'account_service', 'account.db')}"
    os.environ["TRANSACTION_DATABASE_URL"] = f"sqlite:///{os.path.join(base_dir, 'transaction_service', 'transaction.db')}"
    os.environ["REPORTING_DATABASE_URL"] = f"sqlite:///{os.path.join(base_dir, 'reporting_service', 'reporting.db')}"
    
    # Disable Consul service registration for local development
    os.environ["DISABLE_CONSUL"] = "true"
    
    # Setup signal handler
    def signal_handler(sig, frame):
        print('Stopping all services...')
        for process in processes:
            if process.poll() is None:  # If process is still running
                process.terminate()
        sys.exit(0)
    
    signal.signal(signal.SIGINT, signal_handler)
    
    try:
        # Run Auth Service
        if not args.no_auth:
            auth_cmd = [sys.executable, "-m", "auth_service.auth_service"]
            print(f"Starting Auth Service: {' '.join(auth_cmd)}")
            auth_process = subprocess.Popen(auth_cmd)
            processes.append(auth_process)
            time.sleep(1)  # Give it a moment to start
        
        # Run Account Service
        if not args.no_account:
            account_cmd = [sys.executable, "-m", "account_service.account_service"]
            print(f"Starting Account Service: {' '.join(account_cmd)}")
            account_process = subprocess.Popen(account_cmd)
            processes.append(account_process)
            time.sleep(1)  # Give it a moment to start
        
        # Run Transaction Service
        if not args.no_transaction:
            transaction_cmd = [sys.executable, "-m", "transaction_service.transaction_service"]
            print(f"Starting Transaction Service: {' '.join(transaction_cmd)}")
            transaction_process = subprocess.Popen(transaction_cmd)
            processes.append(transaction_process)
            time.sleep(1)  # Give it a moment to start
        
        # Run Reporting Service
        if not args.no_reporting:
            reporting_cmd = [sys.executable, "-m", "reporting_service.reporting_service"]
            print(f"Starting Reporting Service: {' '.join(reporting_cmd)}")
            reporting_process = subprocess.Popen(reporting_cmd)
            processes.append(reporting_process)
            time.sleep(1)  # Give it a moment to start
        
        # Run API Gateway (main.py)
        if not args.no_gateway:
            gateway_cmd = [sys.executable, "main.py"]
            print(f"Starting API Gateway: {' '.join(gateway_cmd)}")
            gateway_process = subprocess.Popen(gateway_cmd)
            processes.append(gateway_process)
        
        # Wait for all processes
        for process in processes:
            process.wait()
            
    except KeyboardInterrupt:
        print('Stopping all services...')
        for process in processes:
            if process.poll() is None:  # If process is still running
                process.terminate()
        sys.exit(0)
    except Exception as e:
        print(f"Error: {e}")
        for process in processes:
            if process.poll() is None:  # If process is still running
                process.terminate()
        sys.exit(1)

def main():
    args = parse_args()
    run_services(args)

if __name__ == "__main__":
    main()