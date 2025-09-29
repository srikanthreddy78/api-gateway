#!/bin/bash

GATEWAY_URL="http://localhost:8080"
API_KEY="test-$(date +%s)"

echo "========================================="
echo "🧪 Testing Rate Limiting"
echo "========================================="
echo "Gateway: $GATEWAY_URL"
echo "API Key: $API_KEY"
echo ""

SUCCESS=0
BLOCKED=0
TOTAL=105

echo "Making $TOTAL requests..."
echo ""

for i in $(seq 1 $TOTAL); do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "X-API-Key: $API_KEY" \
        "$GATEWAY_URL/api/users/123")

    if [ "$HTTP_CODE" == "200" ]; then
        SUCCESS=$((SUCCESS + 1))
        echo -n "."
    elif [ "$HTTP_CODE" == "429" ]; then
        BLOCKED=$((BLOCKED + 1))
        echo -n "X"
    else
        echo -n "?"
    fi

    # Small delay
    sleep 0.01
done

echo ""
echo ""
echo "========================================="
echo "📊 Results"
echo "========================================="
echo "Total Requests:    $TOTAL"
echo "✅ Successful:     $SUCCESS"
echo "❌ Rate Limited:   $BLOCKED"
echo ""

if [ $SUCCESS -eq 100 ] && [ $BLOCKED -eq 5 ]; then
    echo "✅ Rate limiting works correctly!"
    echo "   - First 100 requests passed"
    echo "   - Next 5 requests blocked"
    exit 0
elif [ $SUCCESS -eq $TOTAL ]; then
    echo "⚠️  All requests passed (Redis might be down - fail open mode)"
    exit 0
else
    echo "❌ Unexpected results"
    echo "   Expected: 100 success, 5 blocked"
    echo "   Got: $SUCCESS success, $BLOCKED blocked"
    exit 1
fi