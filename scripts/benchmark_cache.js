/**
 * Benchmark Automatizado de Rendimiento de Caché Redis - Laboratorio 4
 * Ejecuta 50 peticiones consecutivas a GET /api/subjects
 * Calcula el Hit Ratio (porcentaje de aciertos) y latencias con y sin caché.
 *
 * Uso:
 *   node scripts/benchmark_cache.js [GATEWAY_URL]
 *   Ejemplo: node scripts/benchmark_cache.js http://localhost:8080
 */

const GATEWAY_URL = (process.argv[2] || 'http://localhost:8080').replace(/\/$/, '');

async function run() {
  console.log(`=================================================================`);
  console.log(`🚀 Iniciando Benchmark de Caché Redis (UPTC Sistemas Distribuidos)`);
  console.log(`🎯 URL Objetivo: ${GATEWAY_URL}/api/subjects`);
  console.log(`=================================================================\n`);

  // 1. Obtener token de autenticación
  console.log(`🔑 Paso 1: Autenticando usuario de prueba (admin@uptc.edu.co)...`);
  let token = null;
  try {
    const loginRes = await fetch(`${GATEWAY_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: 'admin@uptc.edu.co',
        password: 'Admin123*',
      }),
    });

    if (loginRes.ok) {
      const data = await loginRes.json();
      token = data.token;
      console.log(`✅ Token obtenido con éxito. Rol: ${data.role}\n`);
    } else {
      console.log(`⚠️ Login no exitoso (${loginRes.status}). Continuando sin token (para Gateway en modo bypass)...`);
    }
  } catch (err) {
    console.log(`⚠️ No se pudo conectar a /api/auth/login: ${err.message}. Continuando...`);
  }

  const headers = token ? { Authorization: `Bearer ${token}` } : {};

  // 2. Ejecutar 50 peticiones
  console.log(`📊 Paso 2: Ejecutando 50 peticiones consecutivas a /api/subjects...`);
  const TOTAL = 50;
  const records = [];

  for (let i = 1; i <= TOTAL; i++) {
    const start = performance.now();
    try {
      const res = await fetch(`${GATEWAY_URL}/api/subjects`, { headers });
      const duration = performance.now() - start;
      const xCache = res.headers.get('x-cache') || 'N/A';
      records.push({
        index: i,
        status: res.status,
        duration: Math.round(duration),
        xCache,
      });

      process.stdout.write(`  [${i}/${TOTAL}] HTTP ${res.status} | Latencia: ${Math.round(duration)}ms | X-Cache: ${xCache}\n`);
    } catch (err) {
      const duration = performance.now() - start;
      records.push({
        index: i,
        status: 0,
        duration: Math.round(duration),
        xCache: 'ERR',
      });
      process.stdout.write(`  [${i}/${TOTAL}] Error: ${err.message}\n`);
    }
  }

  // 3. Cálculos de Estadísticas
  const hits = records.filter((r) => r.xCache === 'HIT');
  const misses = records.filter((r) => r.xCache === 'MISS');
  const hitRatio = ((hits.length / TOTAL) * 100).toFixed(1);

  const avgHit = hits.length > 0 ? (hits.reduce((acc, r) => acc + r.duration, 0) / hits.length).toFixed(1) : 0;
  const avgMiss = misses.length > 0 ? (misses.reduce((acc, r) => acc + r.duration, 0) / misses.length).toFixed(1) : 0;
  const speedup = avgHit > 0 ? (avgMiss / avgHit).toFixed(1) : 'N/A';

  console.log(`\n=================================================================`);
  console.log(`📈 RESULTADOS DEL BENCHMARK DE CACHÉ REDIS (50 PETICIONES)`);
  console.log(`=================================================================`);
  console.log(`• Total Peticiones:                 ${TOTAL}`);
  console.log(`• Aciertos de Caché (HIT):           ${hits.length}`);
  console.log(`• Fallos de Caché (MISS):            ${misses.length}`);
  console.log(`• Porcentaje de Aciertos (Hit Ratio): ${hitRatio}%`);
  console.log(`• Latencia promedio SIN caché (MISS): ${avgMiss} ms`);
  console.log(`• Latencia promedio CON caché (HIT):  ${avgHit} ms`);
  console.log(`• Factor de Aceleración:             ${speedup}x`);
  console.log(`=================================================================\n`);

  console.log(`📋 Copia esta tabla en tu informe oficial (lab4_grupoN_azure.md):\n`);
  console.log(`| Métrica | Valor Obtenido |`);
  console.log(`| :--- | :--- |`);
  console.log(`| Total de Peticiones | ${TOTAL} |`);
  console.log(`| Hit Ratio (% aciertos) | **${hitRatio}%** |`);
  console.log(`| Latencia Promedio MISS (Base de Datos) | ${avgMiss} ms |`);
  console.log(`| Latencia Promedio HIT (Redis en Memoria) | ${avgHit} ms |`);
  console.log(`| Factor de Aceleración | **${speedup}x más rápido** |`);
}

run();

