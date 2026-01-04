
 Critical tuning you MUST do (non-negotiable)

A. Limit Spring Boot memory (very important)

Add this to your Dockerfile or docker-compose.yml:

```yaml
environment:
  JAVA_TOOL_OPTIONS: "-Xms256m -Xmx512m"
```

Without this → JVM may try to use more RAM → OOM → container restarts.

⸻

B. Reduce PostgreSQL memory usage

Add to postgres service:

```yaml
environment:
  POSTGRES_SHARED_BUFFERS: 128MB
```

Postgres defaults are too aggressive for 1 GB systems.

⸻

C. Avoid exposing Postgres publicly

You don’t need this line for resume:
    
```yaml
ports:
  - "5432:5432"
```

Remove it. This:
	•	Saves bandwidth
	•	Improves security
	•	Looks more “professional”

⸻

7️⃣ Will this look bad to interviewers?

No — the opposite.

Interviewers care about:
	•	Architecture
	•	Dockerization
	•	Networking
	•	DB integration
	•	Health checks
	•	Practical constraints

They do not care whether your VM is fast.

In fact, saying:

“I deployed this on a free-tier VM and tuned JVM/Postgres memory to fit 1 GB RAM”

is a green flag for backend roles.

⸻

8️⃣ Honest recommendation (based on your profile)

Given:
	•	Resume project
	•	1 YoE
	•	Docker + Spring Boot
	•	No Oracle Cloud access
	•	Want ₹0 cost

Best options ranked:

1️⃣ GCP e2-micro (with tuning) ✅
2️⃣ Fly.io (if app-only, no DB persistence)
3️⃣ Render (free tier sleeps)
4️⃣ Old laptop server (only if you want infra learning)

For resume credibility, GCP VM > hobby server.

⸻

9️⃣ Final verdict (no sugarcoating)
	•	❌ Not good for CI / builds / heavy workloads
	•	❌ Not good for production
	•	✅ Perfectly fine for a resume backend project
	•	✅ Will not cost money if you stay disciplined
	•	✅ Your docker-compose is acceptable with tuning

If you want, next I can:
	•	Rewrite your docker-compose.yml optimized for e2-micro
	•	Give you a “safe checklist” so you never get billed
	•	Help you decide exactly what to say in interviews about this deployment

Just tell me which one you want.