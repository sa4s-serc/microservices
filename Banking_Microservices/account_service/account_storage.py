import os
import json
import logging
from pathlib import Path
from typing import List, Optional, Dict, Any

logger = logging.getLogger(__name__)

class AccountStorage:
    """Storage handler for account service - File-based backup storage"""
    
    def __init__(self, data_dir='./data'):
        """Initialize storage with data directory"""
        self.data_dir = data_dir
        self.accounts_file = os.path.join(data_dir, 'accounts.json')
        
        # Create data directory if it doesn't exist
        os.makedirs(data_dir, exist_ok=True)
        
        # Create accounts file if it doesn't exist
        if not os.path.exists(self.accounts_file):
            with open(self.accounts_file, 'w') as f:
                json.dump([], f)
    
    def get_all_accounts(self) -> List[Dict[str, Any]]:
        """Get all accounts from storage"""
        try:
            with open(self.accounts_file, 'r') as f:
                return json.load(f)
        except Exception as e:
            logger.error(f"Error reading accounts file: {e}")
            return []
    
    def get_account(self, account_id: str) -> Optional[Dict[str, Any]]:
        """Get account by ID"""
        accounts = self.get_all_accounts()
        
        for account in accounts:
            if account.get('id') == account_id:
                return account
        
        return None
    
    def get_account_by_account_number(self, account_number: str) -> Optional[Dict[str, Any]]:
        """Get account by account number"""
        accounts = self.get_all_accounts()
        
        for account in accounts:
            if account.get('account_number') == account_number:
                return account
        
        return None
    
    def get_accounts_by_user_id(self, user_id: str) -> List[Dict[str, Any]]:
        """Get all accounts for a specific user"""
        accounts = self.get_all_accounts()
        
        # Filter accounts by user_id
        user_accounts = [account for account in accounts if account.get('user_id') == user_id]
        
        return user_accounts
    
    def create_account(self, account_data: Dict[str, Any]) -> bool:
        """Create a new account"""
        try:
            accounts = self.get_all_accounts()
            
            # Check for duplicate ID
            for account in accounts:
                if account.get('id') == account_data.get('id'):
                    logger.warning(f"Account with ID {account_data.get('id')} already exists")
                    return False
            
            # Check for duplicate account number
            for account in accounts:
                if account.get('account_number') == account_data.get('account_number'):
                    logger.warning(f"Account with number {account_data.get('account_number')} already exists")
                    return False
            
            accounts.append(account_data)
            
            with open(self.accounts_file, 'w') as f:
                json.dump(accounts, f, indent=2)
            
            logger.info(f"Account created: {account_data.get('id')}")
            return True
            
        except Exception as e:
            logger.error(f"Error writing account data: {e}")
            return False
    
    def update_account(self, account_id: str, update_data: Dict[str, Any]) -> bool:
        """Update an existing account"""
        try:
            accounts = self.get_all_accounts()
            
            for i, account in enumerate(accounts):
                if account.get('id') == account_id:
                    # Update fields
                    for key, value in update_data.items():
                        accounts[i][key] = value
                    
                    with open(self.accounts_file, 'w') as f:
                        json.dump(accounts, f, indent=2)
                    
                    logger.info(f"Account updated: {account_id}")
                    return True
            
            logger.warning(f"Account not found for update: {account_id}")
            return False
            
        except Exception as e:
            logger.error(f"Error updating account data: {e}")
            return False
    
    def delete_account(self, account_id: str) -> bool:
        """Delete an account"""
        try:
            accounts = self.get_all_accounts()
            
            for i, account in enumerate(accounts):
                if account.get('id') == account_id:
                    accounts.pop(i)
                    
                    with open(self.accounts_file, 'w') as f:
                        json.dump(accounts, f, indent=2)
                    
                    logger.info(f"Account deleted: {account_id}")
                    return True
            
            logger.warning(f"Account not found for deletion: {account_id}")
            return False
            
        except Exception as e:
            logger.error(f"Error deleting account data: {e}")
            return False
    
    def backup_accounts(self, accounts_data: List[Dict[str, Any]]) -> bool:
        """Backup accounts data to file storage"""
        try:
            with open(self.accounts_file, 'w') as f:
                json.dump(accounts_data, f, indent=2)
            
            logger.info(f"Backed up {len(accounts_data)} accounts to file storage")
            return True
            
        except Exception as e:
            logger.error(f"Error backing up accounts data: {e}")
            return False
    
    def get_accounts_count(self) -> int:
        """Get total number of accounts in storage"""
        try:
            accounts = self.get_all_accounts()
            return len(accounts)
        except Exception as e:
            logger.error(f"Error getting accounts count: {e}")
            return 0
    
    def search_accounts(self, search_term: str, search_field: str = 'account_number') -> List[Dict[str, Any]]:
        """Search accounts by specific field"""
        try:
            accounts = self.get_all_accounts()
            matching_accounts = []
            
            for account in accounts:
                if search_field in account and search_term.lower() in str(account[search_field]).lower():
                    matching_accounts.append(account)
            
            return matching_accounts
            
        except Exception as e:
            logger.error(f"Error searching accounts: {e}")
            return []