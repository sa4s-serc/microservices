#!/bin/bash

# Train-Ticket Microservices Complexity Report Generator
# Requires: SonarQube running on localhost:9000 with authentication token

TOKEN="squ_90998b2d32009c314dcbf1a16ede3cd4741768ce"
PROJECT_KEY="train-ticket-static"
SONAR_URL="http://localhost:9000"

echo "🎯 TRAIN-TICKET MICROSERVICES - COMPREHENSIVE COMPLEXITY REPORT"
echo "================================================================"
echo "Generated on: $(date)"
echo "SonarQube Project: $PROJECT_KEY"
echo ""

# Check if SonarQube is accessible
if ! curl -s "$SONAR_URL/api/system/status" | grep -q "UP"; then
    echo "❌ Error: SonarQube server not accessible at $SONAR_URL"
    exit 1
fi

echo "✅ SonarQube server is accessible"
echo ""

# Fetch overall project metrics
echo "📊 OVERALL PROJECT METRICS:"
echo "----------------------------"
curl -s -H "Authorization: Bearer $TOKEN" \
    "$SONAR_URL/api/measures/component?component=$PROJECT_KEY&metricKeys=ncloc,complexity,cognitive_complexity,code_smells,files,classes,functions,duplicated_lines_density,coverage" | \
python3 -c "
import json, sys
try:
    data = json.load(sys.stdin)
    if 'component' in data and 'measures' in data['component']:
        measures = {m['metric']: m.get('value', 'N/A') for m in data['component']['measures']}
        
        # Convert to numbers where possible
        ncloc = int(measures.get('ncloc', 0)) if measures.get('ncloc', '0').isdigit() else 0
        complexity = int(measures.get('complexity', 0)) if measures.get('complexity', '0').isdigit() else 0
        cognitive = int(measures.get('cognitive_complexity', 0)) if measures.get('cognitive_complexity', '0').isdigit() else 0
        functions = int(measures.get('functions', 1)) if measures.get('functions', '1').isdigit() else 1
        files = int(measures.get('files', 1)) if measures.get('files', '1').isdigit() else 1
        
        print(f'📁 Total Files: {files:,}')
        print(f'📝 Lines of Code: {ncloc:,}')
        print(f'🔄 Cyclomatic Complexity: {complexity:,}')
        print(f'🧠 Cognitive Complexity: {cognitive:,}')
        print(f'🏗️  Classes: {measures.get(\"classes\", \"N/A\")}')
        print(f'⚙️  Functions: {functions:,}')
        print(f'⚠️  Code Smells: {measures.get(\"code_smells\", \"N/A\")}')
        print(f'📋 Duplicate Code: {measures.get(\"duplicated_lines_density\", \"N/A\")}%')
        
        print()
        print('📈 QUALITY METRICS:')
        print('------------------')
        if functions > 0:
            print(f'🔄 Avg Cyclomatic Complexity per Function: {complexity / functions:.2f}')
            print(f'🧠 Avg Cognitive Complexity per Function: {cognitive / functions:.2f}')
        if files > 0:
            print(f'📏 Avg Lines of Code per File: {ncloc / files:.1f}')
            
        # Quality assessment
        print()
        print('🎖️  QUALITY ASSESSMENT:')
        print('----------------------')
        
        avg_cyclo = complexity / functions if functions > 0 else 0
        avg_cognitive = cognitive / functions if functions > 0 else 0
        
        if avg_cyclo <= 2:
            print('🟢 Cyclomatic Complexity: EXCELLENT (≤2.0)')
        elif avg_cyclo <= 4:
            print('🟡 Cyclomatic Complexity: GOOD (2.0-4.0)')
        elif avg_cyclo <= 6:
            print('🟠 Cyclomatic Complexity: MODERATE (4.0-6.0)')
        else:
            print('🔴 Cyclomatic Complexity: HIGH (>6.0)')
            
        if avg_cognitive <= 1.5:
            print('🟢 Cognitive Complexity: EXCELLENT (≤1.5)')
        elif avg_cognitive <= 3:
            print('🟡 Cognitive Complexity: GOOD (1.5-3.0)')
        elif avg_cognitive <= 5:
            print('🟠 Cognitive Complexity: MODERATE (3.0-5.0)')
        else:
            print('🔴 Cognitive Complexity: HIGH (>5.0)')
            
    else:
        print('❌ Unable to fetch project metrics')
        exit(1)
except Exception as e:
    print(f'❌ Error parsing metrics: {e}')
    exit(1)
"

echo ""
echo "🏗️  TOP 15 MOST COMPLEX MICROSERVICES:"
echo "------------------------------------"

# Fetch component tree and analyze by service
curl -s -H "Authorization: Bearer $TOKEN" \
    "$SONAR_URL/api/measures/component_tree?component=$PROJECT_KEY&metricKeys=complexity,cognitive_complexity,ncloc,functions,files&strategy=children&ps=100" | \
python3 -c "
import json, sys, re
from collections import defaultdict

try:
    data = json.load(sys.stdin)
    microservices = defaultdict(lambda: {'complexity': 0, 'cognitive_complexity': 0, 'ncloc': 0, 'functions': 0, 'files': 0})
    
    if 'components' in data:
        for component in data['components']:
            name = component['name']
            
            # Extract service name
            if '/' in name:
                service_parts = name.split('/')
                for part in service_parts:
                    if part.startswith('ts-'):
                        service_name = part
                        break
                else:
                    service_name = service_parts[0]
            else:
                service_name = name
                
            # Clean up service name
            service_name = re.sub(r'^.*?(ts-[^/]+).*$', r'\1', service_name)
            if not service_name.startswith('ts-'):
                service_name = name.split('/')[-1] if '/' in name else name
                
            # Aggregate measures
            if 'measures' in component:
                for measure in component['measures']:
                    metric = measure['metric']
                    value = int(measure['value']) if measure['value'].isdigit() else 0
                    microservices[service_name][metric] += value
    
    # Sort by complexity and display top 15
    sorted_services = sorted(microservices.items(), key=lambda x: x[1]['complexity'], reverse=True)
    
    print('Rank | Service Name                     | LoC    | CC  | CogC | Funcs | Quality')
    print('-----|----------------------------------|--------|-----|------|-------|--------')
    
    for i, (service, metrics) in enumerate(sorted_services[:15], 1):
        if metrics['ncloc'] > 0:
            complexity_per_func = metrics['complexity'] / max(metrics['functions'], 1)
            cognitive_per_func = metrics['cognitive_complexity'] / max(metrics['functions'], 1)
            
            # Quality indicator
            if complexity_per_func <= 2 and cognitive_per_func <= 1.5:
                quality = '🟢'
            elif complexity_per_func <= 4 and cognitive_per_func <= 3:
                quality = '🟡'
            elif complexity_per_func <= 6 and cognitive_per_func <= 5:
                quality = '🟠'
            else:
                quality = '🔴'
                
            print(f'{i:4d} | {service[:32]:<32} | {metrics[\"ncloc\"]:>6,} | {metrics[\"complexity\"]:>3} | {metrics[\"cognitive_complexity\"]:>4} | {metrics[\"functions\"]:>5} | {quality}')
    
    total_services = len([s for s in microservices.values() if s['ncloc'] > 0])
    if total_services > 15:
        print(f'     | ... and {total_services - 15} more services')
        
except Exception as e:
    print(f'❌ Error analyzing services: {e}')
"

echo ""
echo "📋 SUMMARY & RECOMMENDATIONS:"
echo "----------------------------"
echo "🟢 Green: Excellent complexity (CC≤2, CogC≤1.5)"
echo "🟡 Yellow: Good complexity (CC≤4, CogC≤3)"  
echo "🟠 Orange: Moderate complexity (CC≤6, CogC≤5)"
echo "🔴 Red: High complexity (needs refactoring)"
echo ""
echo "💡 To reduce complexity:"
echo "   - Extract methods from large functions"
echo "   - Reduce nested conditions and loops"
echo "   - Apply design patterns (Strategy, Factory, etc.)"
echo "   - Split large classes into smaller ones"
echo ""
echo "🌐 View full interactive report: $SONAR_URL/dashboard?id=$PROJECT_KEY"
echo "================================================================"