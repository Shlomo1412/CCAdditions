-- CC Additions Scanner Test Script
-- Place this on a computer adjacent to a Scanner block
-- Run with: test_scanner

print("=== CC Additions Scanner Test ===")
print("")

-- Find the scanner peripheral
local scanner = peripheral.find("scanner")

if not scanner then
    print("[FAIL] No scanner peripheral found!")
    print("Make sure a Scanner block is adjacent to this computer.")
    return
end

print("[OK] Scanner peripheral found!")
print("")

-- Test 1: getMaxRange()
print("Test 1: getMaxRange()")
local ok, maxRange = pcall(scanner.getMaxRange)
if ok then
    print("  [OK] Max range: " .. tostring(maxRange))
else
    print("  [FAIL] " .. tostring(maxRange))
end

-- Test 2: getPosition()
print("")
print("Test 2: getPosition()")
local ok, pos = pcall(scanner.getPosition)
if ok and type(pos) == "table" then
    print("  [OK] Position: x=" .. pos.x .. ", y=" .. pos.y .. ", z=" .. pos.z)
else
    print("  [FAIL] " .. tostring(pos))
end

-- Test 3: scan() with small range
print("")
print("Test 3: scan(3, 3, 3)")
local ok, blocks = pcall(scanner.scan, 3, 3, 3)
if ok and type(blocks) == "table" then
    print("  [OK] Found " .. #blocks .. " blocks")
    if #blocks > 0 then
        print("  Sample block: " .. blocks[1].name)
        if blocks[1].metadata then
            print("    hardness: " .. tostring(blocks[1].metadata.hardness))
        end
    end
else
    print("  [FAIL] " .. tostring(blocks))
end

-- Test 4: scanForEntities() with small range
print("")
print("Test 4: scanForEntities(10, 10, 10)")
local ok, entities = pcall(scanner.scanForEntities, 10, 10, 10)
if ok and type(entities) == "table" then
    print("  [OK] Found " .. #entities .. " entities")
    for i, entity in ipairs(entities) do
        if i <= 3 then
            print("  - " .. entity.name .. " (" .. entity.displayName .. ")")
        end
    end
else
    print("  [FAIL] " .. tostring(entities))
end

-- Test 5: Range clamping (should not error with 999)
print("")
print("Test 5: Range clamping (scan with 999)")
local ok, result = pcall(scanner.scan, 1, 1, 999)
if ok then
    print("  [OK] Large range was accepted (clamped to 256)")
else
    print("  [FAIL] " .. tostring(result))
end

print("")
print("=== Test Complete ===")
print("Copy everything above and paste it back!")
