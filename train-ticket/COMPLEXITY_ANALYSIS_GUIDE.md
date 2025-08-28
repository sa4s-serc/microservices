# Train-Ticket Complexity Analysis Guide

This guide shows how to run complexity analysis on your microservices after code changes.

## 🚀 Quick Start

**Run Full Analysis:**
```bash
./complexity_report_generator.sh
```

**Update Token (if needed):**
Edit the token in `complexity_report_generator.sh`:
```bash
TOKEN="your_new_token_here"
```

## 📊 Step-by-Step Analysis

### 1. Run SonarQube Analysis
```bash
# Full analysis with current token
sonar-scanner \
  -Dsonar.token=squ_90998b2d32009c314dcbf1a16ede3cd4741768ce \
  -Dsonar.projectKey=train-ticket-static \
  -Dsonar.projectName="Train Ticket Microservices" \
  -Dsonar.projectVersion=1.0 \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.sources=. \
  -Dsonar.inclusions="**/src/main/java/**/*.java,**/*.py,**/*.go,**/*.js" \
  -Dsonar.exclusions="**/target/**,**/node_modules/**,**/.git/**,**/images/**,**/received/**,**/old-docs/**,**/deployment/**,**/hack/**,**/jenkins-ci/**,**/script/**,**/test/**" \
  -Dsonar.java.source=8 \
  -Dsonar.sourceEncoding=UTF-8 \
  -Dsonar.java.binaries=.
```

### 2. Fetch Complexity Metrics
```bash
# Overall project metrics
curl -H "Authorization: Bearer squ_90998b2d32009c314dcbf1a16ede3cd4741768ce" \
  "http://localhost:9000/api/measures/component?component=train-ticket-static&metricKeys=complexity,cognitive_complexity,ncloc,files,classes,functions,code_smells"

# Detailed per-service metrics
curl -H "Authorization: Bearer squ_90998b2d32009c314dcbf1a16ede3cd4741768ce" \
  "http://localhost:9000/api/measures/component_tree?component=train-ticket-static&metricKeys=complexity,cognitive_complexity,ncloc,functions,files&strategy=children&ps=100"
```

### 3. Generate Complexity Scores
```bash
python3 complexity_scorer.py
```

## 🔧 Prerequisites

### SonarQube Setup
1. **Start SonarQube:**
   ```bash
   docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
   ```

2. **Get Authentication Token:**
   - Open: http://localhost:9000
   - Login: admin/admin
   - Go to: Administration → Security → Users → Tokens
   - Generate token and update scripts

### Dependencies
```bash
# Required tools
which sonar-scanner  # Should return /usr/local/bin/sonar-scanner
which curl           # For API calls
which python3        # For scoring script
```

## 📁 Generated Files

- `complexity_report_generator.sh` - Full automated analysis
- `complexity_scorer.py` - Scoring algorithm
- `microservices_complexity_table.md` - Results table
- `sonar-simple.properties` - SonarQube configuration

## 🎯 Selected Representative Services

Based on complexity scoring formula: `(LoC×0.3) + (Cyclomatic×0.4) + (Cognitive×0.3)`

- 🔴 **BAD:** ts-order-other-service (Score: 70.76)
- 🟡 **AVERAGE:** ts-cancel-service (Score: 36.23)  
- 🟢 **GOOD:** ts-verification-code-service (Score: 15.75)

## 🌐 SonarQube UI Navigation

1. **Project Dashboard:** http://localhost:9000/dashboard?id=train-ticket-static
2. **Measures View:** Click "Measures" → Select "Complexity" metrics
3. **Component Tree:** Browse individual services and files
4. **Code Browser:** Click "Code" → Navigate to specific files

## 📊 API Endpoints Reference

```bash
# System status
GET http://localhost:9000/api/system/status

# Project metrics
GET http://localhost:9000/api/measures/component?component=train-ticket-static&metricKeys=ncloc,complexity,cognitive_complexity

# Component tree
GET http://localhost:9000/api/measures/component_tree?component=train-ticket-static&strategy=children

# Search components
GET http://localhost:9000/api/measures/search?metricKeys=complexity&projectKeys=train-ticket-static
```

## 🔄 Regular Workflow

**After code changes:**
1. Run analysis: `./complexity_report_generator.sh`
2. Check dashboard: http://localhost:9000
3. Focus on services with increased complexity
4. Refactor if scores exceed thresholds:
   - GOOD: 0-30
   - AVERAGE: 31-60  
   - BAD: 61-100

## 🛠 Troubleshooting

**Authentication Error (401):**
- Regenerate token in SonarQube UI
- Update token in scripts

**Analysis Fails:**
- Check SonarQube is running: `docker ps | grep sonar`
- Verify sonar-scanner installed: `which sonar-scanner`

**No Metrics:**
- Wait 30 seconds after analysis
- Check project exists in SonarQube UI
- Verify project key: `train-ticket-static`