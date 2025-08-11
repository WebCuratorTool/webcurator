import type { Annotation } from '@/types/annotation';

interface TargetInstance {
    owner: string,
    harvestDate: number,
    thumbnail: string,
    agency:	string,
    annotations: Array<Annotation>,
    flagId: number,
    amountUrls: number,
    amountCrawls: number,
    qaRecommendation: string,
    name: string,
    dataDownloaded: number,
    id: number,
    state: number,
    runTime: number,
    percentageFailed: number
}

export type { TargetInstance }