import { PrismaClient, Role } from '@prisma/client';
import bcrypt from 'bcryptjs';
import 'dotenv/config';

const prisma = new PrismaClient();

const SERVERS = [
  { id: 1,   name: 'Bokken',   ip: '112.213.84.18',  port: 14444, type: 0 },
  { id: 2,   name: 'Shuriken', ip: '27.0.14.73',     port: 14444, type: 0 },
  { id: 3,   name: 'Tessen',   ip: '27.0.14.73',     port: 14444, type: 1 },
  { id: 4,   name: 'Kunai',    ip: '112.213.94.135', port: 14444, type: 0 },
  { id: 5,   name: 'Katana',   ip: '112.213.94.161', port: 14444, type: 0 },
  { id: 6,   name: 'Tone',     ip: '112.213.94.205', port: 14444, type: 0 },
  { id: 7,   name: 'Sanzu',    ip: '112.213.94.205', port: 14444, type: 1 },
  { id: 8,   name: 'Shensha',  ip: '27.0.14.122',    port: 14444, type: 0 },
  { id: 9,   name: 'Fukiya',   ip: '27.0.12.8',      port: 14444, type: 0 },
  { id: 10,  name: 'Tekkan',   ip: '27.0.12.108',    port: 14445, type: 0 },
  { id: 11,  name: 'Daisho',   ip: '27.0.12.8',      port: 14444, type: 0 },
  { id: 12,  name: 'Bisento',  ip: '27.0.12.11',     port: 14444, type: 0 },
  { id: 100, name: 'World',    ip: '52.221.222.194', port: 14444, type: 0 },
  { id: 102, name: 'NSO247',   ip: '160.30.113.140', port: 14444, type: 0 },
  { id: 103, name: 'GTNS',     ip: '14.225.213.206', port: 14444, type: 0 },
];

async function main() {
  const adminUsername = process.env.ADMIN_USERNAME ?? 'admin';
  const adminPassword = process.env.ADMIN_PASSWORD ?? 'admin@123';

  const hashed = await bcrypt.hash(adminPassword, 10);

  await prisma.user.upsert({
    where: { username: adminUsername },
    update: {},
    create: {
      username: adminUsername,
      password: hashed,
      role: Role.ADMIN,
    },
  });
  console.log(`[seed] admin user "${adminUsername}" ready`);

  for (const s of SERVERS) {
    await prisma.server.upsert({
      where: { id: s.id },
      update: { name: s.name, ip: s.ip, port: s.port, type: s.type },
      create: s,
    });
  }
  console.log(`[seed] ${SERVERS.length} game servers ready`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
