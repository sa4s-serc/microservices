#!/bin/bash

echo "============================================"
echo "SonarQube Analysis for Train-Ticket Project"
echo "============================================"

# Check dependencies
echo "Checking dependencies..."
if ! command -v sonar-scanner &> /dev/null; then
    echo "Error: sonar-scanner not found. Please install SonarScanner."
    exit 1
fi

if ! command -v curl &> /dev/null; then
    echo "Error: curl not found. Please install curl."
    exit 1
fi

# Verify SonarQube is running
echo "Checking SonarQube server..."
if ! curl -s http://localhost:9000/api/system/status | grep -q "UP"; then
    echo "Error: SonarQube server is not running or accessible at http://localhost:9000"
    echo "Please start your SonarQube Docker container."
    exit 1
fi

echo "SonarQube server is running."

# Create a simplified sonar-project.properties for immediate analysis
cat > sonar-project-simple.properties << EOF
sonar.projectKey=train-ticket
sonar.projectName=Train Ticket Microservices
sonar.projectVersion=1.0
sonar.host.url=http://localhost:9000
sonar.sourceEncoding=UTF-8

# Include all Java source files
sonar.sources=.
sonar.inclusions=**/src/main/java/**/*.java,**/*.py,**/*.go,**/*.js
sonar.exclusions=**/target/**,**/node_modules/**,**/.git/**,**/images/**,**/received/**,**/old-docs/**

# Java-specific settings
sonar.java.source=8
sonar.java.target=8
EOF

echo "Running SonarQube analysis..."

# Try different authentication methods
echo "Attempting analysis with default setup..."
sonar-scanner -Dsonar.projectKey=train-ticket -Dsonar.host.url=http://localhost:9000 -Dproject.settings=sonar-project-simple.properties

# Check if analysis was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Analysis completed successfully!"
    echo ""
    echo "📊 To view detailed reports:"
    echo "   - Open: http://localhost:9000/dashboard?id=train-ticket"
    echo "   - Navigate to 'Measures' to see complexity metrics"
    echo ""
    echo "🔍 Key metrics to look for:"
    echo "   - Lines of Code (LoC)"
    echo "   - Cyclomatic Complexity"
    echo "   - Cognitive Complexity"
    echo "   - Code Smells"
    echo "   - Technical Debt"
    echo ""
    
    # Try to fetch basic metrics via API
    echo "Attempting to fetch basic project metrics..."
    
    # Wait a moment for analysis to be processed
    sleep 10
    
    echo ""
    echo "📋 Basic Project Overview:"
    curl -s "http://localhost:9000/api/measures/component?component=train-ticket&metricKeys=ncloc,complexity,cognitive_complexity,code_smells" | \
    python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if 'component' in data and 'measures' in data['component']:
        measures = {m['metric']: m.get('value', 'N/A') for m in data['component']['measures']}
        print('  Lines of Code:', measures.get('ncloc', 'N/A'))
        print('  Cyclomatic Complexity:', measures.get('complexity', 'N/A')) 
        print('  Cognitive Complexity:', measures.get('cognitive_complexity', 'N/A'))
        print('  Code Smells:', measures.get('code_smells', 'N/A'))
    else:
        print('  Metrics not yet available. Please check the SonarQube dashboard.')
except:
    print('  Metrics not yet available. Please check the SonarQube dashboard.')
"
    
else
    echo ""
    echo "❌ Analysis failed. This might be due to:"
    echo "   1. Authentication issues with SonarQube"
    echo "   2. SonarQube server configuration"
    echo "   3. Project configuration issues"
    echo ""
    echo "🔧 Manual steps to resolve:"
    echo "   1. Open http://localhost:9000 in your browser"
    echo "   2. Login with admin/admin (or your credentials)"
    echo "   3. Create a new project manually with key 'train-ticket'"
    echo "   4. Generate an authentication token"
    echo "   5. Re-run analysis with: sonar-scanner -Dsonar.token=YOUR_TOKEN"
fi

echo ""
echo "============================================"