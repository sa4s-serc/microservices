#!/usr/bin/env python3
"""
Microservice Complexity Scorer
Calculates a complexity score based on LoC, Cyclomatic Complexity, and Cognitive Complexity
"""

import math

def calculate_complexity_score(loc, cyclomatic, cognitive):
    """
    Calculate a normalized complexity score (0-100, lower is better)
    
    Formula: 
    - Normalize each metric to 0-100 scale
    - Apply weights: LoC (30%), Cyclomatic (40%), Cognitive (30%)  
    - Higher scores indicate higher complexity (worse maintainability)
    
    Args:
        loc: Lines of Code
        cyclomatic: Cyclomatic Complexity  
        cognitive: Cognitive Complexity
        
    Returns:
        float: Complexity score (0-100, lower is better)
    """
    
    # Normalize metrics (using industry thresholds)
    # LoC: 0-1000 lines (typical microservice range)
    loc_score = min(100, (loc / 1000) * 100)
    
    # Cyclomatic: 0-200 (high complexity threshold)
    cyclo_score = min(100, (cyclomatic / 200) * 100)
    
    # Cognitive: 0-150 (high cognitive load threshold)  
    cognitive_score = min(100, (cognitive / 150) * 100)
    
    # Weighted average (Cyclomatic gets highest weight as most critical)
    weighted_score = (loc_score * 0.3) + (cyclo_score * 0.4) + (cognitive_score * 0.3)
    
    return round(weighted_score, 2)

def classify_service(score):
    """Classify service based on complexity score"""
    if score <= 30:
        return "GOOD"
    elif score <= 60:
        return "AVERAGE" 
    else:
        return "BAD"

# Microservices data (excluding ui-dashboard and ts-common)
services = [
    ("ts-order-service", 923, 131, 100),
    ("ts-order-other-service", 872, 124, 99),
    ("ts-travel-service", 886, 100, 74),
    ("ts-inside-payment-service", 804, 89, 63),
    ("ts-travel2-service", 800, 81, 59),
    ("ts-rebook-service", 559, 74, 76),
    ("ts-basic-service", 552, 65, 71),
    ("ts-wait-order-service", 487, 60, 34),
    ("ts-food-service", 543, 58, 62),
    ("ts-ticket-office-service", 208, 56, 8),
    ("ts-assurance-service", 451, 50, 27),
    ("ts-auth-service", 608, 49, 15),
    ("ts-admin-basic-info-service", 557, 48, 0),
    ("ts-cancel-service", 441, 48, 67),
    ("ts-contacts-service", 392, 46, 21),
    ("ts-admin-travel-service", 385, 44, 31),
    ("ts-station-service", 422, 44, 22),
    ("ts-preserve-other-service", 490, 41, 37),
    ("ts-preserve-service", 479, 41, 40),
    ("ts-admin-order-service", 353, 39, 24),
    ("ts-payment-service", 385, 39, 24),
    ("ts-admin-route-service", 349, 38, 22),
    ("ts-price-service", 410, 38, 16),
    ("ts-route-service", 386, 37, 20),
    ("ts-security-service", 359, 37, 17),
    ("ts-food-delivery-service", 382, 36, 25),
    ("ts-consign-service", 361, 35, 18),
    ("ts-user-service", 326, 35, 13),
    ("ts-admin-user-service", 307, 34, 16),
    ("ts-config-service", 305, 34, 18),
    ("ts-consign-price-service", 320, 33, 14),
    ("ts-delivery-service", 291, 32, 15),
    ("ts-execute-service", 304, 32, 14),
    ("ts-gateway-service", 214, 32, 0),
    ("ts-seat-service", 301, 32, 13),
    ("ts-train-service", 338, 32, 13),
    ("ts-notification-service", 315, 31, 14),
    ("ts-route-plan-service", 282, 31, 13),
    ("ts-station-food-service", 301, 31, 13),
    ("ts-train-food-service", 280, 31, 13),
    ("ts-travel-plan-service", 290, 31, 13),
    ("ts-verification-code-service", 265, 29, 10),
    ("ts-news-service", 30, 5, 0),
    ("ts-avatar-service", 137, 4, 0),
    ("ts-voucher-service", 48, 1, 0),
]

# Calculate scores and classify
results = []
for name, loc, cyclomatic, cognitive in services:
    score = calculate_complexity_score(loc, cyclomatic, cognitive)
    category = classify_service(score)
    results.append((name, loc, cyclomatic, cognitive, score, category))

# Sort by score (highest to lowest complexity)
results.sort(key=lambda x: x[4], reverse=True)

print("🎯 MICROSERVICE COMPLEXITY SCORING RESULTS")
print("=" * 80)
print()
print("Formula: Weighted Score = (LoC*0.3 + Cyclomatic*0.4 + Cognitive*0.3)")
print("Scale: 0-100 (lower is better)")
print("Categories: GOOD (0-30), AVERAGE (31-60), BAD (61-100)")
print()

print(f"{'Rank':<4} {'Service Name':<35} {'LoC':<5} {'CC':<4} {'CogC':<4} {'Score':<6} {'Category'}")
print("-" * 80)

good_services = []
average_services = []
bad_services = []

for i, (name, loc, cyclomatic, cognitive, score, category) in enumerate(results, 1):
    print(f"{i:<4} {name:<35} {loc:<5} {cyclomatic:<4} {cognitive:<4} {score:<6} {category}")
    
    if category == "GOOD":
        good_services.append((name, score))
    elif category == "AVERAGE":
        average_services.append((name, score))
    else:
        bad_services.append((name, score))

print()
print("📊 CATEGORY SUMMARY:")
print(f"🟢 GOOD Services: {len(good_services)}")
print(f"🟡 AVERAGE Services: {len(average_services)}")  
print(f"🔴 BAD Services: {len(bad_services)}")

print()
print("🎯 RECOMMENDED REPRESENTATIVE SERVICES:")
print("-" * 50)

# Select representative services
if bad_services:
    bad_choice = bad_services[len(bad_services)//2]  # Middle of bad services
    print(f"🔴 BAD Representative: {bad_choice[0]} (Score: {bad_choice[1]})")

if average_services:
    avg_choice = average_services[len(average_services)//2]  # Middle of average services  
    print(f"🟡 AVERAGE Representative: {avg_choice[0]} (Score: {avg_choice[1]})")
    
if good_services:
    # Pick a good service that's not too simple (avoid the tiny services)
    good_choice = None
    for name, score in reversed(good_services):  # Start from highest score in good category
        if score > 15:  # Avoid trivially simple services
            good_choice = (name, score)
            break
    if not good_choice:
        good_choice = good_services[-1]  # Fallback to best service
    print(f"🟢 GOOD Representative: {good_choice[0]} (Score: {good_choice[1]})")

print()
print("💡 These three services represent different complexity levels for analysis.")